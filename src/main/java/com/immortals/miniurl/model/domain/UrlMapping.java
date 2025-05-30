package com.immortals.miniurl.model.domain;

import com.immortals.miniurl.model.audit.Auditable;
import com.immortals.miniurl.model.enums.RedirectType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "url_mapping", schema = "mini_url", indexes = {
        @Index(name = "idx_url_mapping_userid", columnList = "user_id"),
        @Index(name = "idx_url_mapping_short_url", columnList = "short_url"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Audited
@EntityListeners(AuditingEntityListener.class)
public class UrlMapping extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "url_mapping_url_mapping_id_seq")
    @SequenceGenerator(name = "url_mapping_url_mapping_id_seq", sequenceName = "mini_url.url_mapping_url_mapping_id_seq", allocationSize = 1)
    @Column(name = "url_mapping_id")
    private Long urlMappingId;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "short_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String shortUrl;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "number_of_clicks", nullable = false, updatable = false)
    private Long numberOfClicks;

    @Column(name = "notes", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String notes;

    @Column(name = "custom_alias_flag", columnDefinition = "TEXT", nullable = false, updatable = false)
    private Boolean customAlias;

    @Column(name = "premium_user_flag", columnDefinition = "TEXT", nullable = false, updatable = false)
    private Boolean premiumUser;

    @Column(name = "high_throughput_flag", columnDefinition = "TEXT", nullable = false, updatable = false)
    private Boolean highThroughput;

    @Column(name = "needs_determinism_flag", columnDefinition = "TEXT", nullable = false, updatable = false)
    private Boolean needsDeterminism;

    @Column(name = "internal_tool_flag", columnDefinition = "TEXT", nullable = false, updatable = false)
    private Boolean internalTool;

    @Column(name = "custom_alias_name", columnDefinition = "TEXT", updatable = false)
    private String customAliasName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "redirect_type", nullable = false)
    private RedirectType redirectType;

    @Column(name = "tags")
    private String tags;

    @Column(name = "strategy")
    private String strategy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_user_agent", length = 512, nullable = false)
    private String createdUserAgent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

}