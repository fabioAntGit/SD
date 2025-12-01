package src.Core.Data.dtos;

import java.util.Map;
import src.Estatisticas.SemaforoStats;

public record SemaforoStatsEvent(Map<String, SemaforoStats.Snapshot> stats) implements DashboardEvent {}
