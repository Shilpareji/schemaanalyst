package org.schemaanalyst.sqlparser;

import java.util.ArrayList;
import java.util.List;

import gudusoft.gsqlparser.EConstraintType;
import gudusoft.gsqlparser.nodes.TConstraint;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TObjectNameList;

import org.schemaanalyst.schema.Column;
import org.schemaanalyst.schema.Schema;
import org.schemaanalyst.schema.Table;

class ConstraintInstaller {

	Schema schema;
	Table currentTable;
	Column currentColumn;
	TConstraint node;
	String constraintName;
	
	ConstraintInstaller(Schema schema) {
		this.schema = schema;
	}
	
	void install(Table currentTable, Column currentColumn, TConstraint node) {
		this.currentTable = currentTable;
		this.currentColumn = currentColumn;
		this.node = node;
		
		TObjectName constraintNameObject = node.getConstraintName();
		if (constraintNameObject != null) {
			constraintName = constraintNameObject.toString();
		}
		
    	EConstraintType constraintType = node.getConstraint_type(); 

    	if (constraintType == EConstraintType.check) {
    		installCheckConstraint();
    	} else if (constraintType == EConstraintType.foreign_key) {
    		installForeignKeyConstraint();
    	} else if (constraintType == EConstraintType.notnull) {
    		installNotNullConstraint();    	
    	} else if (constraintType == EConstraintType.primary_key) {
    		installPrimaryKeyConstraint();
    	} else if (constraintType == EConstraintType.unique) {
    		installUniqueConstraint();
    	} 
	}
		
	List<Column> getConstraintColumns() {
		List<Column> columns = new ArrayList<>();
    	
    	TObjectNameList nodeColumns = node.getColumnList();
    	if (nodeColumns == null) {
    		columns.add(currentColumn);
    	} else {
    		for (int i=0; i < nodeColumns.size(); i++) {
    			String columnName = nodeColumns.getObjectName(i).toString();
    			Column column = currentTable.getColumn(columnName);
    			columns.add(column);
    		}
    	}		
		
		return columns;
	}
	
	void installCheckConstraint() {
		
	}
	
	void installNotNullConstraint() {
		List<Column> columns = getConstraintColumns();
		
		if (columns.size() > 1) {
			throw new ConstraintInstallationException("Cannot make more than one column NOT NULL at a time");
		}
		
		Column column = columns.get(0);
		currentTable.addNotNullConstraint(constraintName, column);
	}
	
	void installPrimaryKeyConstraint() {
		Column[] columns = getConstraintColumns().toArray(new Column[0]);
		currentTable.setPrimaryKeyConstraint(constraintName, columns);
	}
	
	void installForeignKeyConstraint() {
		String referenceTableName = node.getReferencedObject().toString();
		Table referenceTable = schema.getTable(referenceTableName);
		
		List<Column> columns = getConstraintColumns();
		
		TObjectNameList referenceColumnList = node.getReferencedColumnList();
		for (int i=0; i < referenceColumnList.size(); i++) {
			String columnName = referenceColumnList.getObjectName(i).toString();
			Column column = referenceTable.getColumn(columnName);
			columns.add(column);
		}
		
		currentTable.addForeignKeyConstraint(constraintName, referenceTable, columns.toArray(new Column[0]));
	}
	
	void installUniqueConstraint() {
		Column[] columns = getConstraintColumns().toArray(new Column[0]);
		currentTable.addUniqueConstraint(constraintName, columns);		
	}
}
