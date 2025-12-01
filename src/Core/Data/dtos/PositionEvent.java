package src.Core.Data.dtos;

import src.Core.Data.Veiculo;

public record PositionEvent(String nodeName, Veiculo veiculo) implements DashboardEvent {}
