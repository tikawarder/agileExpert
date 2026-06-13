package hu.agilexpert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "icons")
public class Icon extends BaseEntity {

    @ToString.Include
    private String name;

    public Icon() {
        super();
    }

    public Icon(String name) {
        super();
        this.name = name;
    }
}
