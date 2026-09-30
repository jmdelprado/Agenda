-- V2: índices de rendimiento (T071) + corrección de la unicidad de nombre de workspace.
--
-- El constraint uq_workspaces_user_name de V1 es incondicional: impedía reutilizar el nombre de
-- un workspace ya archivado (soft-deleted), aunque la capa de aplicación (CreateWorkspaceService/
-- RenameWorkspaceService) solo exige unicidad entre los espacios de trabajo ACTIVOS
-- (archived_at IS NULL), según data-model.md § Workspace. Se sustituye por un índice único parcial.
ALTER TABLE workspaces DROP CONSTRAINT uq_workspaces_user_name;
CREATE UNIQUE INDEX ux_workspaces_user_name_active ON workspaces (user_id, name) WHERE archived_at IS NULL;

-- Consultas de agenda (GetWorkspaceAgendaService/GetGlobalAgendaService) filtran tareas con
-- due_at no nulo dentro de las columnas de un board; idx_tasks_due_at (V1) ya existe, se añade
-- la variante compuesta con column_id para acelerar el filtrado por tablero.
CREATE INDEX idx_tasks_column_id_due_at ON tasks (column_id, due_at) WHERE due_at IS NOT NULL;

-- Listado de notificaciones (ListNotificationsService) ordena por created_at DESC por usuario,
-- con o sin filtro unreadOnly; idx_notifications_user_id (V1) no cubre el ORDER BY.
CREATE INDEX idx_notifications_user_id_created_at ON notifications (user_id, created_at DESC);

-- ReminderSchedulerAdapter consulta PENDING con trigger_at <= now(); idx_reminders_status_trigger_at
-- (V1) ya es la combinación óptima para esa consulta — no se duplica aquí.
