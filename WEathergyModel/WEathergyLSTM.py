import torch
from torch import nn

device = 'cuda' if torch.cuda.is_available() else 'cpu'


class WEathergyLSTM(nn.Module):
    def __init__(self, lstm_input_size, lstm_hidden_size, lstm_layers, feature_input_size, linear_hidden_size):
        super().__init__()
        self.lstm_hidden_size = lstm_hidden_size
        self.lstm_layers = lstm_layers
        self.lstm = nn.LSTM(lstm_input_size, lstm_hidden_size, lstm_layers, batch_first=True)
        self.combined_linear = nn.Linear(lstm_hidden_size + feature_input_size, linear_hidden_size)
        self.fc = nn.Linear(linear_hidden_size, 1) # output is single number (demand value)

    def forward(self, x_energy, x_weather):
        batch_size = x_energy.size(0)
        h0 = torch.zeros(self.lstm_layers, batch_size, self.lstm_hidden_size).to(device)  # short term memory
        c0 = torch.zeros(self.lstm_layers, batch_size, self.lstm_hidden_size).to(device)  # long term memory
        out, (h_n, _) = self.lstm(x_energy, (h0, c0))
        combined = torch.cat((x_weather, h_n.squeeze(0)), dim=1)
        linear_out = self.combined_linear(combined)
        out = self.fc(linear_out)
        return out