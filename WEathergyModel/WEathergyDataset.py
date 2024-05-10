from torch.utils.data import Dataset
import torch


class WEathergyDataset(Dataset):
    def __init__(self, X_energy, X_weather, y):
        self.X_energy = X_energy
        self.X_weather = X_weather
        self.y = y

    def __len__(self):
        return len(self.X_energy)

    def __getitem__(self, index):
        return self.X_energy[index], self.X_weather[index], self.y[index]

