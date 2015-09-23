package com.robert.dbsplit.util;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.ResultSetMetaData;

public abstract class OrmUtil {
	private static final Logger log = LoggerFactory.getLogger(OrmUtil.class);

	public static String javaClassName2DbTableName(String name) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			if (Character.isUpperCase(name.charAt(i)) && i != 0) {
				sb.append("_");
			}

			sb.append(Character.toUpperCase(name.charAt(i)));

		}
		return sb.toString();
	}

	public static String javaFieldName2DbFieldName(String name) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			if (Character.isUpperCase(name.charAt(i))) {
				sb.append("_");
			}

			sb.append(Character.toUpperCase(name.charAt(i)));

		}
		return sb.toString();
	}

	public static String dbFieldName2JavaFieldName(String name) {
		StringBuilder sb = new StringBuilder();

		boolean lower = true;
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) == '_') {
				lower = false;
				continue;
			}

			if (lower)
				sb.append(Character.toLowerCase(name.charAt(i)));
			else {
				sb.append(Character.toUpperCase(name.charAt(i)));
				lower = true;
			}

		}
		return sb.toString();
	}

	public static String generateParamPlaceholders(int count) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < count; i++) {
			if (i != 0)
				sb.append(",");
			sb.append("?");
		}

		return sb.toString();
	}

	public static <T> T convertRow2Bean(ResultSet rs, Class<T> clazz) {
		try {
			T bean = clazz.newInstance();

			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				int columnType = rsmd.getColumnType(i);
				String columnName = rsmd.getColumnName(i);
				String fieldName = OrmUtil
						.dbFieldName2JavaFieldName(columnName);
				String setterName = ReflectionUtil
						.fieldName2SetterName(fieldName);

				if (columnType == Types.SMALLINT) {
					Method setter = clazz.getMethod(setterName, Enum.class);
					Class<?> enumParamClazz = setter.getParameterTypes()[0]; 
					Method enumParseFactoryMethod = enumParamClazz.getMethod("parse", int.class);
					Object value = enumParseFactoryMethod.invoke(enumParamClazz, rs.getInt(i));
					
					setter.invoke(bean, value);
				} else {
					Class<? extends Object> param = null;
					Object value = null;
					switch (columnType) {
					case Types.VARCHAR:
						param = String.class;
						value = rs.getString(i);
						break;
					case Types.BIGINT:
						param = Long.class;
						value = rs.getLong(i);
						break;
					case Types.INTEGER:
						param = Integer.class;
						value = rs.getInt(i);
						break;
					case Types.DATE:
						param = Date.class;
						value = rs.getDate(i);
						break;
					default:
						log.error("Dbsplit doesn't support column {} type {}.",
								columnName, columnType);
						throw new Exception("Db column not supported.");
					}

					Method setter = clazz.getMethod(setterName, param);
					setter.invoke(bean, value);
				}
			}

			return bean;
		} catch (Exception e) {
			log.error("Fail to operator on ResultSet metadata for clazz {}.",
					clazz);
			log.error("Exception--->", e);
			throw new IllegalStateException(
					"Fail to operator on ResultSet metadata.", e);
		}

		/*
		 * account.setId(rs.getLong("ID"));
		 * account.setClientId(rs.getString("CLIENT_ID"));
		 * account.setClientDesc(rs.getString("CLIENT_DESC"));
		 * account.setBillingPeriod(Account.BillingPeriod
		 * .valueOf(rs.getString("BILLING_PERIOD")));
		 * account.setDefInvInfoId(rs.getLong("DEF_INV_INFO_ID"));
		 * account.setAccumInvAmount(rs .getLong("ACCUM_INV_AMOUNT"));
		 * account.setCapableInvAmount(rs .getLong("CAPABLE_INV_AMOUNT"));
		 * account.setBalance(rs.getLong("BALANCE"));
		 * account.setStatus(Account.Status.parse(rs .getInt("STATUS")));
		 * account.setCompLock(Account.CompLock.parse(rs .getInt("COMP_LOCK")));
		 * account.setLstUpdUser(rs.getString("LST_UPD_USER"));
		 * account.setLstUpdTime(rs.getDate("LST_UPD_TIME")); return account;
		 */

	}
}
