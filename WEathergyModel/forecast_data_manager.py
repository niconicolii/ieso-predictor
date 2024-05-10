from data_managing_utils import *
import torch


def prepare_forecast_data_for_predictions():
    forecast_df = construct_df_from_documents(request_documents(base_url + '/forecast'))
    feature_df = forecast_df.drop('demand', axis=1)
    return torch.tensor(feature_df.to_numpy()).float()

