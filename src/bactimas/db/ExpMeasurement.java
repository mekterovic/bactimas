package bactimas.db;
import java.util.LinkedList;
public class ExpMeasurement {
	
	public boolean isPlottable() {
		return plottable;
	}
	public void setPlottable(boolean numerical) {
		this.plottable = numerical;
	}

	boolean plottable;
	
	
	
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}

	String name, sql;
	int group;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	public static int 	EXP_VAR_GROUP_BUILTIN = 1,
						EXP_VAR_GROUP_USER = 2;
	
	
	public ExpMeasurement(String name, String sql, int group, boolean plottable) {
		super();
		this.name = name;
		this.sql = sql;
		this.group = group;
		this.plottable = plottable;
	}
	
	private static LinkedList<ExpMeasurement> _builtInMeasures;
	
	static {
		
		_builtInMeasures = new LinkedList<ExpMeasurement>();
		
		
		_builtInMeasures.addLast(new ExpMeasurement("exp_name", "exp_name", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("id_experiment", "id_experiment", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("frame_no", "frame_no", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("time_s", "time_s", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("bact_name", "bact_name", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("id_bacteria", "id_bacteria", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("generation", "generation", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("parent", "parent", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("sibling", "sibling", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		//_builtInMeasures.addLast(new ExpMeasurement("progeny", "progeny", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("progeny_a", "progeny_a", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("progeny_b", "progeny_b", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, false));
		_builtInMeasures.addLast(new ExpMeasurement("birth_time", "birth_time", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("division_time", "division_time", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("interdivision_time", "interdivision_time", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("growth_rate", "growth_rate", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("growth_velocity", "growth_velocity", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("area_square_microns", "area_square_microns", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("area_square_microns_growth", "area_square_microns_growth", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("red_mean", "red_mean", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("green_mean", "green_mean", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("blue_mean", "blue_mean", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("red_bg", "red_bg", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("green_bg", "green_bg", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("blue_bg", "blue_bg", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("red_mean_bg_cor", "red_mean_bg_cor", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("green_mean_bg_cor", "green_mean_bg_cor", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("blue_mean_bg_cor", "blue_mean_bg_cor", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("red_ctcf", "red_ctcf", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("green_ctcf", "green_ctcf", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("blue_ctcf", "blue_ctcf", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("red_mean_bg_cor_change", "red_mean_bg_cor_change", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("green_mean_bg_cor_change", "green_mean_bg_cor_change", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("blue_mean_bg_cor_change", "blue_mean_bg_cor_change", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("red_ctcf_change", "red_ctcf_change", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("green_ctcf_change", "green_ctcf_change", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));
		_builtInMeasures.addLast(new ExpMeasurement("blue_ctcf_change", "blue_ctcf_change", ExpMeasurement.EXP_VAR_GROUP_BUILTIN, true));		
				
	}
	
	public static LinkedList<ExpMeasurement> getBuiltInMeasures () {
		return _builtInMeasures;
	} 
	
}
