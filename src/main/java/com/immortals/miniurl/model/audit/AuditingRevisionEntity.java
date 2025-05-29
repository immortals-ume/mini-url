package com.immortals.miniurl.model.audit;

import com.immortals.miniurl.audit.AuditingRevisionListener;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@Table(name = "revinfo",schema = "url")
@Getter
@Setter
@RevisionEntity(AuditingRevisionListener.class)
public class AuditingRevisionEntity extends DefaultRevisionEntity {

    private String username;
}
