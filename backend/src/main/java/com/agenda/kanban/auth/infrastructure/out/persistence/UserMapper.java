package com.agenda.kanban.auth.infrastructure.out.persistence;

import com.agenda.kanban.auth.domain.model.User;

final class UserMapper {

    private UserMapper() {
    }

    static User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getCreatedAt());
    }

    static UserJpaEntity toJpaEntity(User user) {
        return new UserJpaEntity(user.getId(), user.getEmail(), user.getPasswordHash(), user.getCreatedAt());
    }
}
