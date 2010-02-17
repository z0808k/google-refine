package com.metaweb.gridworks.model.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;

import com.metaweb.gridworks.browsing.Engine;
import com.metaweb.gridworks.browsing.FilteredRows;
import com.metaweb.gridworks.browsing.RowVisitor;
import com.metaweb.gridworks.history.HistoryEntry;
import com.metaweb.gridworks.model.Column;
import com.metaweb.gridworks.model.Project;
import com.metaweb.gridworks.model.changes.CellChange;
import com.metaweb.gridworks.model.changes.MassCellChange;
import com.metaweb.gridworks.process.Process;
import com.metaweb.gridworks.process.QuickHistoryEntryProcess;

abstract public class EngineDependentMassCellOperation extends EngineDependentOperation {
	private static final long serialVersionUID = -8962461328087299452L;
	
	final protected String	_columnName;
	final protected boolean _updateRowContextDependencies;
	
	protected EngineDependentMassCellOperation(
	        JSONObject engineConfig, String columnName, boolean updateRowContextDependencies) {
		super(engineConfig);
		_columnName = columnName;
		_updateRowContextDependencies = updateRowContextDependencies;
	}

	public Process createProcess(Project project, Properties options) throws Exception {
		Engine engine = createEngine(project);
		
		Column column = project.columnModel.getColumnByName(_columnName);
		if (column == null) {
			throw new Exception("No column named " + _columnName);
		}
		
		List<CellChange> cellChanges = new ArrayList<CellChange>(project.rows.size());
		
		FilteredRows filteredRows = engine.getAllFilteredRows(false);
		filteredRows.accept(project, createRowVisitor(project, cellChanges));
		
		String description = createDescription(column, cellChanges);
		
		MassCellChange massCellChange = new MassCellChange(cellChanges, column.getCellIndex(), _updateRowContextDependencies);
		HistoryEntry historyEntry = new HistoryEntry(
			project, description, this, massCellChange);

		return new QuickHistoryEntryProcess(project, historyEntry);
	}
	
	abstract protected RowVisitor createRowVisitor(Project project, List<CellChange> cellChanges) throws Exception;
	abstract protected String createDescription(Column column, List<CellChange> cellChanges);
}
