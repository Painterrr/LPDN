package com.fisa.lep.mart.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMart is a Querydsl query type for Mart
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMart extends EntityPathBase<Mart> {

    private static final long serialVersionUID = 2133875536L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMart mart = new QMart("mart");

    public final com.fisa.lep.common.QBaseEntity _super = new com.fisa.lep.common.QBaseEntity(this);

    public final com.fisa.lep.area.entity.QArea area;

    public final StringPath brand = createString("brand");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final BooleanPath isDeleted = _super.isDeleted;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedTime = _super.modifiedTime;

    public final StringPath name = createString("name");

    public QMart(String variable) {
        this(Mart.class, forVariable(variable), INITS);
    }

    public QMart(Path<? extends Mart> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMart(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMart(PathMetadata metadata, PathInits inits) {
        this(Mart.class, metadata, inits);
    }

    public QMart(Class<? extends Mart> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.area = inits.isInitialized("area") ? new com.fisa.lep.area.entity.QArea(forProperty("area")) : null;
    }

}

