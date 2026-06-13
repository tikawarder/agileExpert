package hu.agilexpert.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    protected String id;

    public BaseEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public BaseEntity(String id) {
        this.id = id;
    }
}
