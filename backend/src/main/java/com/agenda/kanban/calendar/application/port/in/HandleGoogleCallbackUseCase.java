package com.agenda.kanban.calendar.application.port.in;

/** Puerto de entrada: procesa el callback de Google (code + state) y guarda la conexión. */
public interface HandleGoogleCallbackUseCase {

    void handleCallback(HandleGoogleCallbackCommand command);

    record HandleGoogleCallbackCommand(String code, String state) {
    }
}
