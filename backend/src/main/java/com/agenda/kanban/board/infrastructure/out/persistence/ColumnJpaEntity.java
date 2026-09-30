package com.agenda.kanban.board.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Mapea la tabla {@code board_columns} (renombrada respecto a la palabra reservada SQL "column"). */
@Entity
@Table(name = "board_columns")
public class ColumnJpaEntity {

    @Id
    private UUID id;

    @Column(name = "board_id", nullable = false)
    private UUID boardId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int position;

    protected ColumnJpaEntity() {
        // requerido por JPA
    }

    public ColumnJpaEntity(UUID id, UUID boardId, String name, int position) {
        this.id = id;
        this.boardId = boardId;
        this.name = name;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}
