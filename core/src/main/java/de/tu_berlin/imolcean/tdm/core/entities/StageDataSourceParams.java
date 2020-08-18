package de.tu_berlin.imolcean.tdm.core.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "STAGE_DS")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class StageDataSourceParams
{
    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true, nullable = false)
    @NonNull
    private String stageName;

    @Column(nullable = false)
    @NonNull
    private String driverClassName;

    @Column(nullable = false)
    @NonNull
    private String url;

    @Column(nullable = false)
    @NonNull
    private String database;

    @Column(nullable = false)
    @NonNull
    private String username;

    @Column(nullable = false)
    @NonNull
    private String password;
}
