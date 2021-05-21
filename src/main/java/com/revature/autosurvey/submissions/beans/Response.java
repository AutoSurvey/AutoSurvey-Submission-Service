package com.revature.autosurvey.submissions.beans;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table("response")
@Data
public class Response {
	@PrimaryKeyColumn(
			name="responseId",
			ordinal=0,
			type = PrimaryKeyType.PARTITIONED,
			ordering = Ordering.DESCENDING) 
	private UUID responseId;
	@PrimaryKeyColumn(
			name="batchName",
			ordinal=1,
			type = PrimaryKeyType.CLUSTERED,
			ordering = Ordering.DESCENDING)
	private String batchName;
	@PrimaryKeyColumn(
			name="week",
			ordinal=2,
			type = PrimaryKeyType.CLUSTERED,
			ordering = Ordering.DESCENDING) 
	private TrainingWeek week;
	@Column 
	private UUID surveyId;
	@Column
	private Map<String, String> surveyResponses;

}
