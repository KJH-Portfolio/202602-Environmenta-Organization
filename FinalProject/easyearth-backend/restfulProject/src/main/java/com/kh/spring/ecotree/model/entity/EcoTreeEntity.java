package com.kh.spring.ecotree.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "ECO_TREE")
@Getter
@Setter
public class EcoTreeEntity {
    
    @Id
    @Column(name = "MEMBER_ID")
    private Integer memberId;
    
    @Column(name = "TREE_LEVEL")
    private Integer treeLevel;
    
    @Column(name = "SYNCED_EXP")
    private Long syncedExp;
    
    @Column(name = "LAST_GROWTH_DATE")
    private LocalDateTime lastGrowthDate;
}
