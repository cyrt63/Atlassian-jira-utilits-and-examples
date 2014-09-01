/*
 * Copyright 2011, Mysema Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.pocketknife.internal.querydsl.tables.domain;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.ForeignKey;
import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.PathMetadataFactory;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import com.mysema.query.types.path.TimePath;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

//@Schema("PUBLIC")
//@Table("EMPLOYEE")
public class QEmployee extends RelationalPathBase<Employee>
{

    private static final long serialVersionUID = 1394463749655231079L;

    public static final QEmployee employee = new QEmployee("EMPLOYEE");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath firstname = createString("firstname");

    public final StringPath lastname = createString("lastname");

    public final NumberPath<BigDecimal> salary = createNumber("salary", BigDecimal.class);

    public final DatePath<Date> datefield = createDate("datefield", Date.class);

    public final TimePath<Time> timefield = createTime("timefield", Time.class);

    public final NumberPath<Integer> superiorId = createNumber("superiorId", Integer.class);

    public final PrimaryKey<Employee> idKey = createPrimaryKey(id);

    public final ForeignKey<Employee> superiorIdKey = createForeignKey(superiorId, "ID");

    public final ForeignKey<Employee> _superiorIdKey = createInvForeignKey(id, "SUPERIOR_ID");

    public QEmployee(String path) {
        super(Employee.class, PathMetadataFactory.forVariable(path), "PUBLIC", "EMPLOYEE");
        addMetadata();
    }

    public QEmployee(PathMetadata<?> metadata) {
        super(Employee.class, metadata, "PUBLIC", "EMPLOYEE");
        addMetadata();
    }

    protected void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID"));
        addMetadata(firstname, ColumnMetadata.named("FIRSTNAME"));
        addMetadata(lastname, ColumnMetadata.named("LASTNAME"));
        addMetadata(salary, ColumnMetadata.named("SALARY"));
        addMetadata(datefield, ColumnMetadata.named("DATEFIELD"));
        addMetadata(timefield, ColumnMetadata.named("TIMEFIELD"));
        addMetadata(superiorId, ColumnMetadata.named("SUPERIOR_ID"));
    }

}
