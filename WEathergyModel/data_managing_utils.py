from copy import deepcopy
from datetime import datetime, timedelta
import pandas as pd
import numpy as np
import json
import pytz
import requests
from sklearn.preprocessing import MinMaxScaler

with open('config.json', 'r') as file:
    config = json.load(file)
    est_timezone = pytz.timezone(config['est_timezone'])
    base_url = config['base_url']
    batch_size = config['batch_size']


# def get_documents(host, port, replicaset, database, collection):
#     if replicaset is None:
#         client = MongoClient(host, port)
#     else:
#         client = MongoClient(host, port, replicaset=replicaset)
#     db = client[database]
#     return db[collection].find({})

def request_documents(url):
    raw_data = requests.get(url).text
    return json.loads(raw_data)


def construct_df_from_documents(all_documents):
    # columns = ['year', 'month', 'day', 'hour', 'demand', 'toronto', 'thunder_bay', 'ottawa', 'timmins']
    columns = ['month', 'hour', 'demand', 'toronto', 'thunder_bay', 'ottawa', 'timmins']
    index = []
    raw_data_frame = dict()
    for key in columns:
        raw_data_frame[key] = []

    for doc in all_documents:
        timestamp = doc.get('dt')
        ldt = datetime.utcfromtimestamp(timestamp).replace(tzinfo=pytz.utc).astimezone(est_timezone)
        index.append(timestamp)
        # raw_data_frame['year'].append(ldt.year)
        raw_data_frame['month'].append(ldt.month)
        # raw_data_frame['day'].append(ldt.day)
        raw_data_frame['hour'].append(ldt.hour)
        raw_data_frame['demand'].append(doc.get('demand', None))
        raw_data_frame['toronto'].append(doc.get('toronto_temp'))
        raw_data_frame['thunder_bay'].append(doc.get('thunder_bay_temp'))
        raw_data_frame['ottawa'].append(doc.get('ottawa_temp'))
        raw_data_frame['timmins'].append(doc.get('timmins_temp'))
    return pd.DataFrame(raw_data_frame, index=index)


############################ manage scaling the datasets ############################

def get_scaled_np(df):
    lstm_np = df.to_numpy()
    scalar = MinMaxScaler(feature_range=(-1, 1))
    return scalar.fit_transform(lstm_np), scalar


def invert_to_actual_demands(data, scalar, n_steps=24):
    """ Need to invert predicted output back to actual demand values since scaled into (-1, 1) """
    dummies = np.zeros((len(data), n_steps + 1))  # same shape as matrix used for MinMaxScaler
    dummies[:, 0] = data.flatten()
    dummies = scalar.inverse_transform(dummies)
    return deepcopy(dummies[:, 0])