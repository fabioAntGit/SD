package src.Core.Data.dtos;

public record SemaforoStateEvent(String semaforoId, boolean ativo, int fila) implements DashboardEvent {}
