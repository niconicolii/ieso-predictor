import json

import torch
from torch import nn
from WEathergy_data_organizer import *
from forecast_data_manager import *
import matplotlib.pyplot as plt
from WEathergyLSTM import WEathergyLSTM

INFO_ON = False


def train_one_epoch(train_loader, model, loss_function, optimizer):
    model.train(True)   # set to training mode
    running_loss = 0.0
    for batch_i, batch in enumerate(train_loader):
        # got batch using __getitem__ from train_loader ==> batch = (X_energy, X_weather, y)
        x_energy_batch = batch[0].to(device)
        x_weather_batch = batch[1].to(device)
        y_batch = batch[2].to(device)

        output = model(x_energy_batch, x_weather_batch)
        loss = loss_function(output, y_batch)
        running_loss += loss
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()

        if batch_i % 100 == 99 and INFO_ON:
            print(f"Batch {batch_i+1}, Loss: {running_loss / 100:2f}")
            running_loss = 0.0
    return model


def validate_one_epoch(test_loader, model, loss_function):
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


def train_and_test(train_loader, test_loader,
                   model, loss_function, optimizer):
    model = train_one_epoch(train_loader, model, loss_function, optimizer)
    validate_one_epoch(test_loader, model, loss_function)
    return model


def load_configs(filename):
    with open(filename, 'r') as file:
        config = json.load(file)
    return config['hyperparameters']


def main():
    hyper = load_configs('config.json')

    # scalar is passed in for future inverts
    data_sets, scalar = get_weathergy_train_test_sets(hyper['n_steps'], hyper['train_set_perc'])
    train_loader = data_to_dataloader_for_lstm(data_sets[0], data_sets[2], data_sets[4], shuffle=True)
    test_loader = data_to_dataloader_for_lstm(data_sets[1], data_sets[3], data_sets[5], shuffle=False)

    model = WEathergyLSTM(lstm_input_size=1,
                          lstm_hidden_size=4,
                          lstm_layers=1,
                          feature_input_size=data_sets[2].shape[1],
                          linear_hidden_size=4)
    model.to(device)
    loss_function = nn.MSELoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=hyper['learning_rate'])

    for epoch in range(hyper['num_epochs']):
        model = train_and_test(train_loader, test_loader,
                               model, loss_function, optimizer)

    # predict_and_plot(data_sets[0], data_sets[2], data_sets[4], model, scalar)
    # predict_and_plot(data_sets[1], data_sets[3], data_sets[5], model, scalar)

    X_energy_curr = torch.cat((data_sets[1][-1][1:], data_sets[5][-1].unsqueeze(0)))
    forecast_mng = ForecastDataManager()
    X_features_forecast = forecast_mng.get_tensor_for_pred()

    forecast_num = X_features_forecast.shape[0]
    preds = torch.zeros(forecast_num)
    with torch.inference_mode():
        for i in range(forecast_num):
            preds[i] = model(X_energy_curr.unsqueeze(0).to(device),
                             X_features_forecast[i].unsqueeze(0).to(device))
            X_energy_curr = torch.cat((X_energy_curr[1:], preds[i].unsqueeze(0).unsqueeze(0)))
    # print(f"hahahahahhahahahaha {preds=}")
    energy_preds = invert_to_actual_demands(preds, scalar)
    plt.plot(energy_preds, label='Predicted Demand')
    # plt.plot(invert_to_actual_demands(y, scalar), label='Actual Demand')
    plt.xlabel('Hours')
    plt.ylabel('Demand')
    plt.legend()
    plt.show()

    forecast_times = forecast_mng.get_timestamps()
    timestamp_to_energy_pred = {forecast_times[i]: int(energy_preds[i].round(decimals=0)) for i in range(len(preds))}
    print(f"{timestamp_to_energy_pred=}")


if __name__ == "__main__":
    device = 'cuda' if torch.cuda.is_available() else 'cpu'

    main()


