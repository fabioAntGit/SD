package src.Core.Data.dtos;

import src.Core.Data.Veiculo;

public record MovementEvent(String semaforoId, String destino, Veiculo veiculo) implements DashboardEvent {}
