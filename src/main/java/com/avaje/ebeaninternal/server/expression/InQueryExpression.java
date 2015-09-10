package com.avaje.ebeaninternal.server.expression;

import java.util.List;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.query.CQuery;

/**
 * In expression using a sub query.
 */
class InQueryExpression extends AbstractExpression {

  private static final long serialVersionUID = 666990277309851644L;

  private final boolean not;

  private final SpiQuery<?> subQuery;

  private transient CQuery<?> compiledSubQuery;

  public InQueryExpression(String propertyName, SpiQuery<?> subQuery, boolean not) {
    super(propertyName);
    this.subQuery = subQuery;
    this.not = not;
  }

  public void queryAutoTuneHash(HashQueryPlanBuilder builder) {
    builder.add(InQueryExpression.class).add(propName).add(not);
    subQuery.queryAutoTuneHash(builder);
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {

    // queryPlanHash executes prior to addSql() or addBindValues()
    // ... so compiledQuery will exist
    compiledSubQuery = compileSubQuery(request);

    queryAutoTuneHash(builder);
  }

  /**
   * Compile/build the sub query.
   */
  private CQuery<?> compileSubQuery(BeanQueryRequest<?> queryRequest) {

    SpiEbeanServer ebeanServer = (SpiEbeanServer) queryRequest.getEbeanServer();
    return ebeanServer.compileQuery(subQuery, queryRequest.getTransaction());
  }

  public int queryBindHash() {
    return subQuery.queryBindHash();
  }

  public void addSql(SpiExpressionRequest request) {

    String subSelect = compiledSubQuery.getGeneratedSql();
    subSelect = subSelect.replace('\n', ' ');

    String propertyName = getPropertyName();
    request.append(" (").append(propertyName).append(")");
    if (not) {
      request.append(" not");
    }
    request.append(" in (");
    request.append(subSelect);
    request.append(") ");
  }

  public void addBindValues(SpiExpressionRequest request) {

    List<Object> bindParams = compiledSubQuery.getPredicates().getWhereExprBindValues();

    if (bindParams == null) {
      return;
    }

    for (int i = 0; i < bindParams.size(); i++) {
      request.addBindValue(bindParams.get(i));
    }
  }
}
