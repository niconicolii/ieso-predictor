import numpy as np

from data_managing_utils import *
import torch
from torch.utils.data import DataLoader
from WEathergyDataset import WEathergyDataset

with open('config.json', 'r') as file:
    config = json.load(file)
    est_timezone = pytz.timezone(config['est_timezone'])
    base_url = config['base_url']
    batch_size = config['batch_size']


def get_no_demand_start_dt():
    """ Calculate the start time of when it is allowed to have an empty demand value. """
    start_dt = datetime.now(tz=est_timezone)
    if start_dt.hour <= 8:
        start_dt = start_dt - timedelta(days=1)
    return start_dt.replace(hour=1, minute=0, second=0, microsecond=0)


def get_five_min_data_from_start(start_dt):
    # since we are getting fiveMin data for calculating hourly demand, we will need extra data 30 mins before start_dt
    start_dt_w_extra = start_dt - timedelta(minutes=30)
    url = '/fiveMin?start=' + start_dt_w_extra.strftime('%Y-%m-%dT%H:%M:%S')
    return request_documents(url)


def calculate_hourly_demand(fiveMin_demand):
    hourly_demands = []
    every_hour = []
    for idx, info in enumerate(fiveMin_demand):
        every_hour.append(info.get("demandValue"))
        if len(every_hour) == 12:
            hourly_demands.append(round(sum(every_hour) / 12))
            every_hour = []
    if every_hour:
        hourly_demands.append(round(sum(every_hour) / len(every_hour)))
    return hourly_demands


def fill_weathergy_w_est_demands(df):
    """
        Since hourly demand values are updated on a daily basis, there will be missing demand values in WEathergyDB.
        Use five-minute demand values (updated every 5 minutes) to calculate estimated hourly demand values for modeling.
    """
    start_dt = get_no_demand_start_dt()
    allow_no_demand_df = deepcopy(df[df.index >= int(start_dt.timestamp())])
    fiveMin_demand = get_five_min_data_from_start(start_dt)
    calculated_demands = calculate_hourly_demand(fiveMin_demand)

    # print(f"{len(calculated_demands)=}  {len(allow_no_demand_df)=}")
    for i in range(len(allow_no_demand_df)):
        dt = allow_no_demand_df.index[i]
        if i >= len(calculated_demands) or calculated_demands[i] == 0:
            allow_no_demand_df.at[dt, 'demand'] = 0
        else:
            allow_no_demand_df.at[dt, 'demand'] = calculated_demands[i]
    return allow_no_demand_df[allow_no_demand_df['demand'] != 0]


def prepare_df_for_lstm(df, n_steps):
    print(f"???????????? {df}")
    df = deepcopy(df)
    demands = df['demand']
    df.drop('demand', axis=1, inplace=True)
    df.insert(0, 'demand', demands)
    energy_list = ['demand']
    for j in range(1, n_steps + 1):
        col_name = f'demand(h-{j})'
        energy_list.append(col_name)
        df[col_name] = df['demand'].shift(j)
    # remove NaN values
    print(f"???????????? before dropna {df}")
    df.dropna(inplace=True)
    energy_df = deepcopy(df[energy_list])
    feature_df = df.drop(energy_list, axis=1)
    print(f"???????????? after dropna {df}\n{energy_df=} \n{feature_df=}")
    return energy_df, feature_df


def _train_test_formatter(X, features_np, y, n_steps):
    return torch.tensor(X.reshape((-1, n_steps, 1))).float(), \
           torch.tensor(features_np).float(), \
           torch.tensor(y.reshape((-1, 1))).float()


def split_n_format_train_test_sets(energy_np, features_np, n_steps, train_percent):
    X = deepcopy(np.flip(energy_np[:, 1:], axis=1))
    y = energy_np[:, 0]
    # print(X.shape, y.shape)
    if train_percent == 1:
        train_data_sets = _train_test_formatter(X, features_np, y, n_steps)
        test_data_sets = [None, None, None]
    else:
        split_index = int(len(X) * (1 - train_percent))
        # LSTM requires an extra dimension at the end => reshape
        train_data_sets = _train_test_formatter(X[split_index:], features_np[split_index:],
                                                         y[split_index:], n_steps)
        test_data_sets = _train_test_formatter(X[:split_index], features_np[:split_index],
                                                         y[:split_index], n_steps)
    data_sets = [train_data_sets[0], test_data_sets[0],
                 train_data_sets[1], test_data_sets[1],
                 train_data_sets[2], test_data_sets[2]]
    return data_sets


def get_weathergy_train_test_sets(df, n_steps=24, train_percent=0.9, scalar=None):
    # # get weathergy data from request, organize into dataframe, and recover missing data
    # df = construct_df_from_documents(request_documents('/weathergy'))
    # finalized_df = df[df['demand'] != 0]
    # calculated_df = fill_weathergy_w_est_demands(df[df['demand'] == 0])

    # separate weathergy data into energy dataframe and feature dataframe for LSTM modeling
    energy_df, feature_df = prepare_df_for_lstm(df, n_steps)

    # scale values into (-1, 1) for easier calculation
    if scalar is None:
        energy_np, scalar = get_scaled_np(energy_df)
    else:
        energy_np = get_scaled_np(energy_df, scalar)

    # separate data into training and testing sets
    data_sets = split_n_format_train_test_sets(energy_np, feature_df.to_numpy(), n_steps=n_steps, train_percent=train_percent)

    return data_sets, scalar


# def prepare_data_for_lstm(X_energy_train, X_energy_test,
#                           X_weather_train, X_weather_test,
#                           y_train, y_test):
#     train_dataset = WEathergyDataset(X_energy_train, X_weather_train, y_train)
#     test_dataset = WEathergyDataset(X_energy_test, X_weather_test, y_test)
#     train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
#     test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False)
#     return train_loader, test_loader


def data_to_dataloader_for_lstm(X_energy, X_features, y, shuffle):
    dataset = WEathergyDataset(X_energy, X_features, y)
    return DataLoader(dataset, batch_size=batch_size, shuffle=shuffle)
