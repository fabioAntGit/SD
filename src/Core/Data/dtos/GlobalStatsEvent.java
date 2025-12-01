package src.Core.Data.dtos;

import src.Estatisticas.GlobalStats;

public record GlobalStatsEvent(GlobalStats.Snapshot snapshot) implements DashboardEvent {}
