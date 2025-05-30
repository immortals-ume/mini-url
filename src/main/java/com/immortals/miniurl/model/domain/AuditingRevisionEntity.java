package com.immortals.miniurl.model.domain;

import com.immortals.miniurl.audit.AuditingRevisionListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "revinfo", schema = "mini_url")
@Getter
@Setter
@RevisionEntity(AuditingRevisionListener.class)
public class AuditingRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revinfo_seq_gen")
    @SequenceGenerator(
            name = "revinfo_seq_gen",
            sequenceName = "mini_url.revinfo_seq",
            allocationSize = 50
    )
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    @Column(nullable = false)
    private long timestamp;

    private String username;
}
