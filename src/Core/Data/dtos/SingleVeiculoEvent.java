package src.Core.Data.dtos;

import src.Core.Data.Veiculo;

public record SingleVeiculoEvent(Veiculo veiculo) implements DashboardEvent {}
