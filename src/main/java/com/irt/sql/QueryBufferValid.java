/*
 *	File Name:	QueryBufferValid.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/05/31		2.2.3	QueryBufferValid.Join 오류 수정
 *	stghr12		2009/06/30		2.2.2	QueryBufferValid.ConditionValue 추가
 *	stghr12		2009/01/31		2.2.1	QueryBufferValid.ConditionTrue 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.2	QueryBufferValid.Condition, QueryBufferValid.GroupKey, QueryBufferValid.Join final로 수정
 *										QueryBufferValid.Basis 추가
 *										QueryBufferValid.Join: isAndCondition 추가
 *	stghr12		2007/04/30		2.1.1	CONDITION_OR, CONDITION_AND 추가
 *										생성자 QueryBufferValid.GroupKey(groupKeys), QueryBufferValid.GroupKey(groupKeys, isAndCondition) 추가
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.sql;

/**
 * QueryBuffer 유효성 검증.
 * {@link ConditionalQueryable}, {@link ConditionalQueryableField}등에서 사용.
 */
public interface QueryBufferValid {
	public final static boolean CONDITION_OR			= false;
	public final static boolean CONDITION_AND			= true;

	public boolean hasValidCondition( QueryBuffer querybuf );

	/**
	 *
	 */
	public final static class Basis implements QueryBufferValid {
		String basisValue;

		public Basis( String basisValue ) {
			this.basisValue = basisValue;
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				Object value = ((ConditionQueryBuffer)querybuf).getConditionValue( com.irt.data.Condition.BASIS_CONDITIONKEY );
				return basisValue.equals( value );
			}

			return false;
		}
	}

	/**
	 *
	 */
	public final static class Condition implements QueryBufferValid {
		String[] conditionKeys;
		boolean isAndCondition;

		public Condition( String... conditionKeys ) {
			this.conditionKeys = conditionKeys;
			this.isAndCondition = CONDITION_OR;
		}

		public Condition( boolean isAndCondition, String... conditionKeys ) {
			this.conditionKeys = conditionKeys;
			this.isAndCondition = isAndCondition;
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
				if( isAndCondition ) {
					for( String conditionKey : conditionKeys )
						if( !condquerybuf.hasConditionValue(conditionKey) )
							return false;

					return true;
				} else {
					for( String conditionKey : conditionKeys )
						if( condquerybuf.hasConditionValue(conditionKey) )
							return true;

					return false;
				}
			} else
				return false;
		}
	}

	/**
	 *
	 */
	public final static class ConditionTrue implements QueryBufferValid {
		String[] conditionKeys;
		boolean isAndCondition;

		public ConditionTrue( String... conditionKeys ) {
			this.conditionKeys = conditionKeys;
			this.isAndCondition = CONDITION_OR;
		}

		public ConditionTrue( boolean isAndCondition, String... conditionKeys ) {
			this.conditionKeys = conditionKeys;
			this.isAndCondition = isAndCondition;
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
				if( isAndCondition ) {
					for( String conditionKey : conditionKeys )
						if( !condquerybuf.isConditionTrue(conditionKey) )
							return false;

					return true;
				} else {
					for( String conditionKey : conditionKeys )
						if( condquerybuf.isConditionTrue(conditionKey) )
							return true;

					return false;
				}
			} else
				return false;
		}
	}

	/**
	 *
	 */
	public final static class ConditionValue implements QueryBufferValid {
		String conditionKey;
		String[] conditionValues;

		public ConditionValue( String conditionKey, String... conditionValues ) {
			this.conditionKey = conditionKey;
			this.conditionValues = conditionValues;
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				Object conditionValue = ((ConditionQueryBuffer)querybuf).getConditionValue( conditionKey );

				if( conditionValue != null ) {
					for( int i = 0; i < conditionValues.length; i++ )
						if( conditionValue.equals(conditionValues[i]) )
							return true;
				}
			}

			return false;
		}
	}

	/**
	 *
	 */
	public final static class GroupKey implements QueryBufferValid {
		String[] groupKeys;
		boolean isAndCondition;

		public GroupKey( String... groupKeys ) {
			this.groupKeys = groupKeys;
			this.isAndCondition = CONDITION_OR;
		}

		public GroupKey( boolean isAndCondition, String... groupKeys ) {
			this.groupKeys = groupKeys;
			this.isAndCondition = isAndCondition;
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer )
				return com.irt.data.Condition.containsGroupKey( ((ConditionQueryBuffer)querybuf).getConditionMap(), groupKeys, isAndCondition );
			else
				return false;
		}
	}

	/**
	 *
	 */
	public final static class Join implements QueryBufferValid {
		QueryBufferValid[] querybufValids;
		boolean isAndCondition;

		public Join( QueryBufferValid... querybufValids ) {
			this( CONDITION_AND, querybufValids );
		}

		public Join( boolean isAndCondition, QueryBufferValid... querybufValids ) {
			int length = 0;
			for( QueryBufferValid querybufValid : querybufValids ) {
				if( querybufValid instanceof QueryBufferValid.Join ) {
					QueryBufferValid.Join querybufValid_tmp = (QueryBufferValid.Join)querybufValid;
					if( querybufValid_tmp.isAndCondition != isAndCondition ) {
						this.querybufValids = querybufValids;
						this.isAndCondition = isAndCondition;
						return;
					}
					length += querybufValid_tmp.querybufValids.length;
				} else
					length++;
			}

			this.isAndCondition = isAndCondition;
			if( length == querybufValids.length )
				this.querybufValids = querybufValids;
			else {
				this.querybufValids = new QueryBufferValid[ length ];

				length = 0;
				for( QueryBufferValid querybufValid : querybufValids ) {
					if( querybufValid instanceof QueryBufferValid.Join ) {
						QueryBufferValid[] querybufValids_tmp = ((QueryBufferValid.Join)querybufValid).querybufValids;
						System.arraycopy( querybufValids_tmp, 0, this.querybufValids, length, querybufValids_tmp.length );

						length += querybufValids_tmp.length;
					} else
						this.querybufValids[length++] = querybufValid;
				}
			}
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( isAndCondition ) {
				for( QueryBufferValid querybufValid : querybufValids )
					if( !querybufValid.hasValidCondition(querybuf) )
						return false;

				return true;
			} else {
				for( QueryBufferValid querybufValid : querybufValids )
					if( querybufValid.hasValidCondition(querybuf) )
						return true;

				return false;
			}
		}
	}
}
