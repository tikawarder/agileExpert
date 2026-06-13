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
@Table(name = "themes")
public class Theme extends BaseEntity {

    @ToString.Include
    private String name;

    public Theme() {
        super();
    }

    public Theme(String name) {
        super();
        this.name = name;
    }
}
