from data_managing_utils import *
import torch


class ForecastDataManager:
    def __init__(self):
        forecast_df = construct_df_from_documents(request_documents('/forecast'))
        self.feature_df = forecast_df.drop('demand', axis=1)
        self.tensor_for_pred = torch.tensor(self.feature_df.to_numpy()).float()

    def get_feature_df(self):
        return self.feature_df

    def get_tensor_for_pred(self):
        return self.tensor_for_pred

    def get_timestamps(self):
        return self.feature_df.index.tolist()

    # def save_demand
