package src.Core.Data.dtos;

import src.Estatisticas.VeiculoStats;

public record VeiculoStatsEvent(VeiculoStats.Snapshot snapshot) implements DashboardEvent {}
