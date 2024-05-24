import os
import json
import joblib
import torch
from torch import nn
from WEathergy_data_organizer import *
from forecast_data_manager import *
import matplotlib.pyplot as plt
from sklearn.model_selection import ParameterGrid

from WEathergyLSTM import WEathergyLSTM


INFO_ON = False


def train_one_epoch(train_loader, model_info):
    model_info['model'].train(True)   # set to training mode
    running_loss = 0.0
    for batch_i, batch in enumerate(train_loader):
        # got batch using __getitem__ from train_loader ==> batch = (X_energy, X_weather, y)
        x_energy_batch = batch[0].to(device)
        x_weather_batch = batch[1].to(device)
        y_batch = batch[2].to(device)

        output = model_info['model'](x_energy_batch, x_weather_batch)
        loss = model_info['loss_function'](output, y_batch)
        running_loss += loss
        model_info['optimizer'].zero_grad()
        loss.backward()
        model_info['optimizer'].step()

        if batch_i % 100 == 99 and INFO_ON:
            print(f"Batch {batch_i+1}, Loss: {running_loss / 100:2f}")
            running_loss = 0.0
    return model_info


def validate_one_epoch(test_loader, model_info):
    model = model_info['model']
    loss_function = model_info['loss_function']
    model.train(False)
    running_loss = 0.0
    for batch_i, batch in enumerate(test_loader):
        x_energy_batch = batch[0].to(device)
        x_weather_batch = batch[1].to(device)
        y_batch = batch[2].to(device)
        with torch.inference_mode():    # non training mode
            output = model(x_energy_batch, x_weather_batch)
            loss = loss_function(output, y_batch)
            running_loss += loss
    avg_loss = running_loss / len(test_loader)
    print(f"Test Loss: {avg_loss:2f}")
    print("==========================================")


def predict_and_plot(X_energy, X_weather, y, model, scalar):
    with torch.inference_mode():
        preds = model(X_energy.to(device), X_weather.to(device)).to('cpu').numpy()
    plt.plot(invert_to_actual_demands(preds, scalar), label='Predicted Demand')
    plt.plot(invert_to_actual_demands(y, scalar), label='Actual Demand')
    plt.xlabel('Hours')
    plt.ylabel('Demand')
    plt.legend()
    plt.show()


def train_and_test(train_loader, test_loader, model_info):
    model_info = train_one_epoch(train_loader, model_info)
    validate_one_epoch(test_loader, model_info)
    return model_info


def load_configs(filename):
    with open(filename, 'r') as file:
        configs = json.load(file)
    return configs


def select_hyperparams(feature_input_size, train_loader):
    print(f'[INFO] Evaluating hyperparameters')

    best_loss = float('inf')
    best_params = None

    param_grid = {
        'lstm_input_size': [1],
        'lstm_hidden_size': [4, 8, 16, 32],
        'lstm_layers': [1],
        'feature_input_size': [feature_input_size],
        'linear_hidden_size': [4, 8, 16],
        'learning_rate': [0.001, 0.005, 0.01],
        'num_epochs': [5, 10, 20]
    }

    for params in ParameterGrid(param_grid):
        print(f'[INFO] Evaluating loss for {params=}')
        model = WEathergyLSTM(params['lstm_input_size'],
                              params['lstm_hidden_size'],
                              params['lstm_layers'],
                              params['feature_input_size'],
                              params['linear_hidden_size'])
        model.to(device)
        loss_function = nn.MSELoss()
        model_info = {
            'model': model,
            'loss_function': loss_function,
            'optimizer': torch.optim.Adam(model.parameters(), lr=params['learning_rate'])
        }
        for epoch in range(params['num_epochs']):
            train_one_epoch(train_loader, model_info)
        model.eval()
        running_loss = 0
        for batch_i, batch in enumerate(train_loader):
            x_energy_batch = batch[0].to(device)
            x_weather_batch = batch[1].to(device)
            y_batch = batch[2].to(device)
            with torch.inference_mode():
                output = model(x_energy_batch, x_weather_batch)
                loss = loss_function(output, y_batch)
                running_loss += loss
        avg_loss = running_loss / len(train_loader)
        print(f'[INFO]       average loss = {avg_loss}')
        if avg_loss < best_loss:
            best_loss = avg_loss
            best_params = params

    print(f'[INFO] Best Loss: {best_loss} \nBest Params: {best_params}')
    return best_params


def train_model(df, n_steps, train_set_perc, model_info=None):
    if model_info is None:
        data_sets, scalar = get_weathergy_train_test_sets(df, n_steps, train_set_perc)
        train_loader = data_to_dataloader_for_lstm(data_sets[0], data_sets[2], data_sets[4], shuffle=True)
        test_loader = data_to_dataloader_for_lstm(data_sets[1], data_sets[3], data_sets[5], shuffle=False)

        # params = select_hyperparams(data_sets[2].shape[1], train_loader)
        # TODO: remove this, using this for debugging right now
        params = {'feature_input_size': 6,
                  'learning_rate': 0.005,
                  'linear_hidden_size': 4,
                  'lstm_hidden_size': 8,
                  'lstm_input_size': 1,
                  'lstm_layers': 1,
                  'num_epochs': 1}
        model_info = init_model_opt_lf(params)
        model_info['scalar'] = scalar
        model_info['test_set'] = [data_sets[1], data_sets[3], data_sets[5]]
    else:
        # if model_info exist that means we are doing re-training, we have a saved test set in model_info
        data_sets, scalar = get_weathergy_train_test_sets(df,
                                                          n_steps=n_steps,
                                                          train_percent=train_set_perc,
                                                          scalar=model_info['scalar'])
        train_loader = data_to_dataloader_for_lstm(data_sets[0], data_sets[2], data_sets[4], shuffle=True)
        test_sets = model_info['test_set']
        test_loader = data_to_dataloader_for_lstm(test_sets[0], test_sets[1], test_sets[2], shuffle=False)

    for epoch in range(model_info['params']['num_epochs']):
        model_info = train_and_test(train_loader, test_loader, model_info)
    return model_info, data_sets


def init_model_opt_lf(params):
    model = WEathergyLSTM(params['lstm_input_size'],
                          params['lstm_hidden_size'],
                          params['lstm_layers'],
                          params['feature_input_size'],
                          params['linear_hidden_size'])
    model.to(device)
    return {
        'model': model,
        'loss_function': nn.MSELoss(),
        'optimizer': torch.optim.Adam(model.parameters(), lr=params['learning_rate']),
        'params': params
    }


def save_model_info(model_info):
    os.makedirs('model', exist_ok=True)
    torch.save(model_info['model'].state_dict(), 'model/model.pth')
    torch.save(model_info['optimizer'].state_dict(), 'model/optimizer.pth')
    torch.save(model_info['test_set'], 'model/test_set.pt')
    joblib.dump(model_info['scalar'], 'model/scalar.pkl')

    with open('model/params.json', 'w') as f:
        json.dump(model_info['params'], f)


def load_existing_model():
    with open('model/params.json', 'r') as f:
        params = json.load(f)
    model_info = init_model_opt_lf(params)
    model_info['model'].load_state_dict(torch.load('model/model.pth'))
    model_info['optimizer'].load_state_dict(torch.load('model/optimizer.pth'))
    model_info['test_set'] = torch.load('model/test_set.pt')
    model_info['scalar'] = joblib.load('model/scalar.pkl')
    return model_info


def main():
    configs = load_configs('config.json')
    n_steps, train_set_perc = configs['n_steps'], configs['train_set_perc']

    # 0. try to get existing model
    #### if model exist, only retrieve WEathergy Data after model's last trained data
    model_info = None
    try:
        model_info = load_existing_model()
    except FileNotFoundError as e:
        print(f"[INFO] No existing model, need to create new model: {e}")
    except OSError as e:
        print(f"[INFO] Error opening existing model, need to create new model: {e}")

    # 1. get df from bff
    print(f'[INFO] Getting WEathergy Data from Database')
    if model_info is None:
        df = construct_df_from_documents(request_documents('/all-weathergy'))
    else:
        last_epoch = model_info['params']['last_epoch']
        df = construct_df_from_documents(request_documents(f'/conditional-weathergy?after={last_epoch}'))
    finalized_df = deepcopy(df[df['demand'] != 0])
    calculated_df = fill_weathergy_w_est_demands(df[df['demand'] == 0])

    # 2. first do modeling with finalized data, and we will save this model
    #### scalar is passed in for future inverts
    if not finalized_df.empty:
        print(f'[INFO] Training model with finalized data')
        model_info, data_sets = train_model(finalized_df, n_steps, train_set_perc, model_info)
        model_info['params']['last_epoch'] = int(finalized_df.index.max())
        save_model_info(model_info)

    # 3. then we calculate demand data for "today"s data = not updated yet in IESO public repo
    # and use this data to continue training, however the new trained model is not saved
    # last n_steps row of finalized_df will be used for constructing lstm datasets for calculated demands
    if len(finalized_df) < 24:
        before_epoch = calculated_df.index.min()
        after_epoch = before_epoch - n_steps * 3600 - 1
        last_24_df = construct_df_from_documents(
            request_documents(f'/conditional-weathergy?after={after_epoch}&before={before_epoch}')
        )
        concat_calculated_df = pd.concat([last_24_df, calculated_df])
    else:
        concat_calculated_df = pd.concat([finalized_df.tail(n_steps), calculated_df])
    concat_calculated_df.to_csv("heihei.csv", encoding='utf-8')
    # calculated_df needs to concatenate with previous n_step datas for constructing LSTM format
    cal_model_info, data_sets = train_model(concat_calculated_df,
                                            n_steps=n_steps,
                                            train_set_perc=1,
                                            model_info=model_info)

    ### predict_and_plot(data_sets[0], data_sets[2], data_sets[4], model, scalar)
    ### predict_and_plot(data_sets[1], data_sets[3], data_sets[5], model, scalar)

    # 4. get weather forecast and predict
    #### data_sets[0][-1][1:] => last row of X_energy_train's [T-23, T-22, T-21, ... , T-2, T-1]
    #### data_sets[4][-1] => last demand value of y_train == T-0
    #### #### use training data since using [...test, ...train] order when splitting train/test sets
    X_energy_curr = torch.cat((data_sets[0][-1][1:], data_sets[4][-1].unsqueeze(0)))
    forecast_mng = ForecastDataManager()
    X_features_forecast = forecast_mng.get_tensor_for_pred()

    forecast_num = X_features_forecast.shape[0]
    preds = torch.zeros(forecast_num)
    with torch.inference_mode():
        for i in range(forecast_num):
            preds[i] = cal_model_info['model'](X_energy_curr.unsqueeze(0).to(device),
                                               X_features_forecast[i].unsqueeze(0).to(device))
            X_energy_curr = torch.cat((X_energy_curr[1:], preds[i].unsqueeze(0).unsqueeze(0)))
    # print(f"hahahahahhahahahaha {preds=}")
    energy_preds = invert_to_actual_demands(preds, model_info['scalar'])

    forecast_times = forecast_mng.get_timestamps()
    timestamp_to_energy_pred = {forecast_times[i]: int(energy_preds[i].round(decimals=0)) for i in range(len(preds))}
    print(f"{timestamp_to_energy_pred=}")
    post_request("/save-energy-predictions", timestamp_to_energy_pred)


if __name__ == "__main__":
    device = 'cuda' if torch.cuda.is_available() else 'cpu'
    main()


