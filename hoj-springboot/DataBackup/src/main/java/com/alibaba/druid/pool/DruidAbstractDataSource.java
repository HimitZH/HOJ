//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alibaba.druid.pool;

import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.FilterChainImpl;
import com.alibaba.druid.filter.FilterManager;
import com.alibaba.druid.pool.vendor.NullExceptionSorter;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.proxy.jdbc.TransactionInfo;
import com.alibaba.druid.stat.JdbcDataSourceStat;
import com.alibaba.druid.stat.JdbcSqlStat;
import com.alibaba.druid.stat.JdbcStatManager;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.DruidPasswordCallback;
import com.alibaba.druid.util.Histogram;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.util.MySqlUtils;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.druid.util.Utils;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.sql.DataSource;

public abstract class DruidAbstractDataSource extends WrapperAdapter implements DruidAbstractDataSourceMBean, DataSource, DataSourceProxy, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(DruidAbstractDataSource.class);
    public static final int DEFAULT_INITIAL_SIZE = 0;
    public static final int DEFAULT_MAX_ACTIVE_SIZE = 8;
    public static final int DEFAULT_MAX_IDLE = 8;
    public static final int DEFAULT_MIN_IDLE = 0;
    public static final int DEFAULT_MAX_WAIT = -1;
    public static final String DEFAULT_VALIDATION_QUERY = null;
    public static final boolean DEFAULT_TEST_ON_BORROW = false;
    public static final boolean DEFAULT_TEST_ON_RETURN = false;
    public static final boolean DEFAULT_WHILE_IDLE = true;
    public static final long DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 60000L;
    public static final long DEFAULT_TIME_BETWEEN_CONNECT_ERROR_MILLIS = 500L;
    public static final int DEFAULT_NUM_TESTS_PER_EVICTION_RUN = 3;
    public static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 1800000L;
    public static final long DEFAULT_MAX_EVICTABLE_IDLE_TIME_MILLIS = 25200000L;
    public static final long DEFAULT_PHY_TIMEOUT_MILLIS = -1L;
    protected volatile boolean defaultAutoCommit = true;
    protected volatile Boolean defaultReadOnly;
    protected volatile Integer defaultTransactionIsolation;
    protected volatile String defaultCatalog = null;
    protected String name;
    protected volatile String username;
    protected volatile String password;
    protected volatile String jdbcUrl;
    protected volatile String driverClass;
    protected volatile ClassLoader driverClassLoader;
    protected volatile Properties connectProperties = new Properties();
    protected volatile PasswordCallback passwordCallback;
    protected volatile NameCallback userCallback;
    protected volatile int initialSize = 0;
    protected volatile int maxActive = 8;
    protected volatile int minIdle = 0;
    protected volatile int maxIdle = 8;
    protected volatile long maxWait = -1L;
    protected int notFullTimeoutRetryCount = 0;
    protected volatile String validationQuery;
    protected volatile int validationQueryTimeout;
    protected volatile boolean testOnBorrow;
    protected volatile boolean testOnReturn;
    protected volatile boolean testWhileIdle;
    protected volatile boolean poolPreparedStatements;
    protected volatile boolean sharePreparedStatements;
    protected volatile int maxPoolPreparedStatementPerConnectionSize;
    protected volatile boolean inited;
    protected volatile boolean initExceptionThrow;
    protected PrintWriter logWriter;
    protected List<Filter> filters;
    private boolean clearFiltersEnable;
    protected volatile ExceptionSorter exceptionSorter;
    protected Driver driver;
    protected volatile int queryTimeout;
    protected volatile int transactionQueryTimeout;
    protected long createTimespan;
    protected volatile int maxWaitThreadCount;
    protected volatile boolean accessToUnderlyingConnectionAllowed;
    protected volatile long timeBetweenEvictionRunsMillis;
    protected volatile int numTestsPerEvictionRun;
    protected volatile long minEvictableIdleTimeMillis;
    protected volatile long maxEvictableIdleTimeMillis;
    protected volatile long keepAliveBetweenTimeMillis;
    protected volatile long phyTimeoutMillis;
    protected volatile long phyMaxUseCount;
    protected volatile boolean removeAbandoned;
    protected volatile long removeAbandonedTimeoutMillis;
    protected volatile boolean logAbandoned;
    protected volatile int maxOpenPreparedStatements;
    protected volatile List<String> connectionInitSqls;
    protected volatile String dbType;
    protected volatile long timeBetweenConnectErrorMillis;
    protected volatile ValidConnectionChecker validConnectionChecker;
    protected final Map<DruidPooledConnection, Object> activeConnections;
    protected static final Object PRESENT = new Object();
    protected long id;
    protected int connectionErrorRetryAttempts;
    protected boolean breakAfterAcquireFailure;
    protected long transactionThresholdMillis;
    protected final Date createdTime;
    protected Date initedTime;
    protected volatile long errorCount;
    protected volatile long dupCloseCount;
    protected volatile long startTransactionCount;
    protected volatile long commitCount;
    protected volatile long rollbackCount;
    protected volatile long cachedPreparedStatementHitCount;
    protected volatile long preparedStatementCount;
    protected volatile long closedPreparedStatementCount;
    protected volatile long cachedPreparedStatementCount;
    protected volatile long cachedPreparedStatementDeleteCount;
    protected volatile long cachedPreparedStatementMissCount;
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> errorCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "errorCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> dupCloseCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "dupCloseCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> startTransactionCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "startTransactionCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> commitCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "commitCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> rollbackCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "rollbackCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> cachedPreparedStatementHitCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "cachedPreparedStatementHitCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> preparedStatementCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "preparedStatementCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> closedPreparedStatementCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "closedPreparedStatementCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> cachedPreparedStatementCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "cachedPreparedStatementCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> cachedPreparedStatementDeleteCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "cachedPreparedStatementDeleteCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> cachedPreparedStatementMissCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "cachedPreparedStatementMissCount");
    protected final Histogram transactionHistogram;
    private boolean dupCloseLogEnable;
    private ObjectName objectName;
    protected volatile long executeCount;
    protected volatile long executeQueryCount;
    protected volatile long executeUpdateCount;
    protected volatile long executeBatchCount;
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> executeQueryCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "executeQueryCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> executeUpdateCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "executeUpdateCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> executeBatchCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "executeBatchCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> executeCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "executeCount");
    protected volatile Throwable createError;
    protected volatile Throwable lastError;
    protected volatile long lastErrorTimeMillis;
    protected volatile Throwable lastCreateError;
    protected volatile long lastCreateErrorTimeMillis;
    protected volatile long lastCreateStartTimeMillis;
    protected boolean isOracle;
    protected boolean isMySql;
    protected boolean useOracleImplicitCache;
    protected ReentrantLock lock;
    protected Condition notEmpty;
    protected Condition empty;
    protected ReentrantLock activeConnectionLock;
    protected volatile int createErrorCount;
    protected volatile int creatingCount;
    protected volatile int directCreateCount;
    protected volatile long createCount;
    protected volatile long destroyCount;
    protected volatile long createStartNanos;
    static final AtomicIntegerFieldUpdater<DruidAbstractDataSource> createErrorCountUpdater = AtomicIntegerFieldUpdater.newUpdater(DruidAbstractDataSource.class, "createErrorCount");
    static final AtomicIntegerFieldUpdater<DruidAbstractDataSource> creatingCountUpdater = AtomicIntegerFieldUpdater.newUpdater(DruidAbstractDataSource.class, "creatingCount");
    static final AtomicIntegerFieldUpdater<DruidAbstractDataSource> directCreateCountUpdater = AtomicIntegerFieldUpdater.newUpdater(DruidAbstractDataSource.class, "directCreateCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> createCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "createCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> destroyCountUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "destroyCount");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> createStartNanosUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "createStartNanos");
    private Boolean useUnfairLock;
    private boolean useLocalSessionState;
    protected long timeBetweenLogStatsMillis;
    protected DruidDataSourceStatLogger statLogger;
    private boolean asyncCloseConnectionEnable;
    protected int maxCreateTaskCount;
    protected boolean failFast;
    protected volatile int failContinuous;
    protected volatile long failContinuousTimeMillis;
    protected ScheduledExecutorService destroyScheduler;
    protected ScheduledExecutorService createScheduler;
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> failContinuousTimeMillisUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "failContinuousTimeMillis");
    static final AtomicIntegerFieldUpdater<DruidAbstractDataSource> failContinuousUpdater = AtomicIntegerFieldUpdater.newUpdater(DruidAbstractDataSource.class, "failContinuous");
    protected boolean initVariants;
    protected boolean initGlobalVariants;
    protected volatile boolean onFatalError;
    protected volatile int onFatalErrorMaxActive;
    protected volatile int fatalErrorCount;
    protected volatile int fatalErrorCountLastShrink;
    protected volatile long lastFatalErrorTimeMillis;
    protected volatile String lastFatalErrorSql;
    protected volatile Throwable lastFatalError;
    protected volatile long connectionIdSeed;
    protected volatile long statementIdSeed;
    protected volatile long resultSetIdSeed;
    protected volatile long transactionIdSeed;
    protected volatile long metaDataIdSeed;
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> connectionIdSeedUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "connectionIdSeed");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> statementIdSeedUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "statementIdSeed");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> resultSetIdSeedUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "resultSetIdSeed");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> transactionIdSeedUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "transactionIdSeed");
    static final AtomicLongFieldUpdater<DruidAbstractDataSource> metaDataIdSeedUpdater = AtomicLongFieldUpdater.newUpdater(DruidAbstractDataSource.class, "metaDataIdSeed");

    public DruidAbstractDataSource(boolean lockFair) {
        this.validationQuery = DEFAULT_VALIDATION_QUERY;
        this.validationQueryTimeout = -1;
        this.testOnBorrow = false;
        this.testOnReturn = false;
        this.testWhileIdle = true;
        this.poolPreparedStatements = false;
        this.sharePreparedStatements = false;
        this.maxPoolPreparedStatementPerConnectionSize = 10;
        this.inited = false;
        this.initExceptionThrow = true;
        this.logWriter = new PrintWriter(System.out);
        this.filters = new CopyOnWriteArrayList();
        this.clearFiltersEnable = true;
        this.exceptionSorter = null;
        this.maxWaitThreadCount = -1;
        this.accessToUnderlyingConnectionAllowed = true;
        this.timeBetweenEvictionRunsMillis = 60000L;
        this.numTestsPerEvictionRun = 3;
        this.minEvictableIdleTimeMillis = 1800000L;
        this.maxEvictableIdleTimeMillis = 25200000L;
        this.keepAliveBetweenTimeMillis = 120000L;
        this.phyTimeoutMillis = -1L;
        this.phyMaxUseCount = -1L;
        this.removeAbandonedTimeoutMillis = 300000L;
        this.maxOpenPreparedStatements = -1;
        this.timeBetweenConnectErrorMillis = 500L;
        this.validConnectionChecker = null;
        this.activeConnections = new IdentityHashMap();
        this.connectionErrorRetryAttempts = 1;
        this.breakAfterAcquireFailure = false;
        this.transactionThresholdMillis = 0L;
        this.createdTime = new Date();
        this.errorCount = 0L;
        this.dupCloseCount = 0L;
        this.startTransactionCount = 0L;
        this.commitCount = 0L;
        this.rollbackCount = 0L;
        this.cachedPreparedStatementHitCount = 0L;
        this.preparedStatementCount = 0L;
        this.closedPreparedStatementCount = 0L;
        this.cachedPreparedStatementCount = 0L;
        this.cachedPreparedStatementDeleteCount = 0L;
        this.cachedPreparedStatementMissCount = 0L;
        this.transactionHistogram = new Histogram(new long[]{1L, 10L, 100L, 1000L, 10000L, 100000L});
        this.dupCloseLogEnable = false;
        this.executeCount = 0L;
        this.executeQueryCount = 0L;
        this.executeUpdateCount = 0L;
        this.executeBatchCount = 0L;
        this.isOracle = false;
        this.isMySql = false;
        this.useOracleImplicitCache = true;
        this.activeConnectionLock = new ReentrantLock();
        this.createErrorCount = 0;
        this.creatingCount = 0;
        this.directCreateCount = 0;
        this.createCount = 0L;
        this.destroyCount = 0L;
        this.createStartNanos = 0L;
        this.useUnfairLock = null;
        this.useLocalSessionState = true;
        this.statLogger = new DruidDataSourceStatLoggerImpl();
        this.asyncCloseConnectionEnable = false;
        this.maxCreateTaskCount = 3;
        this.failFast = false;
        this.failContinuous = 0;
        this.failContinuousTimeMillis = 0L;
        this.initVariants = false;
        this.initGlobalVariants = false;
        this.onFatalError = false;
        this.onFatalErrorMaxActive = 0;
        this.fatalErrorCount = 0;
        this.fatalErrorCountLastShrink = 0;
        this.lastFatalErrorTimeMillis = 0L;
        this.lastFatalErrorSql = null;
        this.lastFatalError = null;
        this.connectionIdSeed = 10000L;
        this.statementIdSeed = 20000L;
        this.resultSetIdSeed = 50000L;
        this.transactionIdSeed = 60000L;
        this.metaDataIdSeed = 80000L;
        this.lock = new ReentrantLock(lockFair);
        this.notEmpty = this.lock.newCondition();
        this.empty = this.lock.newCondition();
    }

    public boolean isUseLocalSessionState() {
        return this.useLocalSessionState;
    }

    public void setUseLocalSessionState(boolean useLocalSessionState) {
        this.useLocalSessionState = useLocalSessionState;
    }

    public DruidDataSourceStatLogger getStatLogger() {
        return this.statLogger;
    }

    public void setStatLoggerClassName(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            DruidDataSourceStatLogger statLogger = (DruidDataSourceStatLogger)clazz.newInstance();
            this.setStatLogger(statLogger);
        } catch (Exception var4) {
            throw new IllegalArgumentException(className, var4);
        }
    }

    public void setStatLogger(DruidDataSourceStatLogger statLogger) {
        this.statLogger = statLogger;
    }

    public long getTimeBetweenLogStatsMillis() {
        return this.timeBetweenLogStatsMillis;
    }

    public void setTimeBetweenLogStatsMillis(long timeBetweenLogStatsMillis) {
        this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
    }

    public boolean isOracle() {
        return this.isOracle;
    }

    public void setOracle(boolean isOracle) {
        if (this.inited) {
            throw new IllegalStateException();
        } else {
            this.isOracle = isOracle;
        }
    }

    public boolean isUseUnfairLock() {
        return this.lock.isFair();
    }

    public void setUseUnfairLock(boolean useUnfairLock) {
        if (this.lock.isFair() != !useUnfairLock) {
            if (!this.inited) {
                ReentrantLock lock = this.lock;
                lock.lock();

                try {
                    if (!this.inited) {
                        this.lock = new ReentrantLock(!useUnfairLock);
                        this.notEmpty = this.lock.newCondition();
                        this.empty = this.lock.newCondition();
                        this.useUnfairLock = useUnfairLock;
                    }
                } finally {
                    lock.unlock();
                }
            }

        }
    }

    public boolean isUseOracleImplicitCache() {
        return this.useOracleImplicitCache;
    }

    public void setUseOracleImplicitCache(boolean useOracleImplicitCache) {
        if (this.useOracleImplicitCache != useOracleImplicitCache) {
            this.useOracleImplicitCache = useOracleImplicitCache;
            boolean isOracleDriver10 = this.isOracle() && this.driver != null && this.driver.getMajorVersion() == 10;
            if (isOracleDriver10 && useOracleImplicitCache) {
                this.getConnectProperties().setProperty("oracle.jdbc.FreeMemoryOnEnterImplicitCache", "true");
            } else {
                this.getConnectProperties().remove("oracle.jdbc.FreeMemoryOnEnterImplicitCache");
            }
        }

    }

    public Throwable getLastCreateError() {
        return this.lastCreateError;
    }

    public Throwable getLastError() {
        return this.lastError;
    }

    public long getLastErrorTimeMillis() {
        return this.lastErrorTimeMillis;
    }

    public Date getLastErrorTime() {
        return this.lastErrorTimeMillis <= 0L ? null : new Date(this.lastErrorTimeMillis);
    }

    public long getLastCreateErrorTimeMillis() {
        return this.lastCreateErrorTimeMillis;
    }

    public Date getLastCreateErrorTime() {
        return this.lastCreateErrorTimeMillis <= 0L ? null : new Date(this.lastCreateErrorTimeMillis);
    }

    public int getTransactionQueryTimeout() {
        return this.transactionQueryTimeout <= 0 ? this.queryTimeout : this.transactionQueryTimeout;
    }

    public void setTransactionQueryTimeout(int transactionQueryTimeout) {
        this.transactionQueryTimeout = transactionQueryTimeout;
    }

    public long getExecuteCount() {
        return this.executeCount + this.executeQueryCount + this.executeUpdateCount + this.executeBatchCount;
    }

    public long getExecuteUpdateCount() {
        return this.executeUpdateCount;
    }

    public long getExecuteQueryCount() {
        return this.executeQueryCount;
    }

    public long getExecuteBatchCount() {
        return this.executeBatchCount;
    }

    public long getAndResetExecuteCount() {
        return executeCountUpdater.getAndSet(this, 0L) + executeQueryCountUpdater.getAndSet(this, 0L) + executeUpdateCountUpdater.getAndSet(this, 0L) + executeBatchCountUpdater.getAndSet(this, 0L);
    }

    public long getExecuteCount2() {
        return this.executeCount;
    }

    public void incrementExecuteCount() {
        executeCountUpdater.incrementAndGet(this);
    }

    public void incrementExecuteUpdateCount() {
        ++this.executeUpdateCount;
    }

    public void incrementExecuteQueryCount() {
        ++this.executeQueryCount;
    }

    public void incrementExecuteBatchCount() {
        ++this.executeBatchCount;
    }

    public boolean isDupCloseLogEnable() {
        return this.dupCloseLogEnable;
    }

    public void setDupCloseLogEnable(boolean dupCloseLogEnable) {
        this.dupCloseLogEnable = dupCloseLogEnable;
    }

    public ObjectName getObjectName() {
        return this.objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public Histogram getTransactionHistogram() {
        return this.transactionHistogram;
    }

    public void incrementCachedPreparedStatementCount() {
        cachedPreparedStatementCountUpdater.incrementAndGet(this);
    }

    public void decrementCachedPreparedStatementCount() {
        cachedPreparedStatementCountUpdater.decrementAndGet(this);
    }

    public void incrementCachedPreparedStatementDeleteCount() {
        cachedPreparedStatementDeleteCountUpdater.incrementAndGet(this);
    }

    public void incrementCachedPreparedStatementMissCount() {
        cachedPreparedStatementMissCountUpdater.incrementAndGet(this);
    }

    public long getCachedPreparedStatementMissCount() {
        return this.cachedPreparedStatementMissCount;
    }

    public long getCachedPreparedStatementAccessCount() {
        return this.cachedPreparedStatementMissCount + this.cachedPreparedStatementHitCount;
    }

    public long getCachedPreparedStatementDeleteCount() {
        return this.cachedPreparedStatementDeleteCount;
    }

    public long getCachedPreparedStatementCount() {
        return this.cachedPreparedStatementCount;
    }

    public void incrementClosedPreparedStatementCount() {
        closedPreparedStatementCountUpdater.incrementAndGet(this);
    }

    public long getClosedPreparedStatementCount() {
        return this.closedPreparedStatementCount;
    }

    public void incrementPreparedStatementCount() {
        preparedStatementCountUpdater.incrementAndGet(this);
    }

    public long getPreparedStatementCount() {
        return this.preparedStatementCount;
    }

    public void incrementCachedPreparedStatementHitCount() {
        cachedPreparedStatementHitCountUpdater.incrementAndGet(this);
    }

    public long getCachedPreparedStatementHitCount() {
        return this.cachedPreparedStatementHitCount;
    }

    public long getTransactionThresholdMillis() {
        return this.transactionThresholdMillis;
    }

    public void setTransactionThresholdMillis(long transactionThresholdMillis) {
        this.transactionThresholdMillis = transactionThresholdMillis;
    }

    public abstract void logTransaction(TransactionInfo var1);

    public long[] getTransactionHistogramValues() {
        return this.transactionHistogram.toArray();
    }

    public long[] getTransactionHistogramRanges() {
        return this.transactionHistogram.getRanges();
    }

    public long getCommitCount() {
        return this.commitCount;
    }

    public void incrementCommitCount() {
        commitCountUpdater.incrementAndGet(this);
    }

    public long getRollbackCount() {
        return this.rollbackCount;
    }

    public void incrementRollbackCount() {
        rollbackCountUpdater.incrementAndGet(this);
    }

    public long getStartTransactionCount() {
        return this.startTransactionCount;
    }

    public void incrementStartTransactionCount() {
        startTransactionCountUpdater.incrementAndGet(this);
    }

    public boolean isBreakAfterAcquireFailure() {
        return this.breakAfterAcquireFailure;
    }

    public void setBreakAfterAcquireFailure(boolean breakAfterAcquireFailure) {
        this.breakAfterAcquireFailure = breakAfterAcquireFailure;
    }

    public int getConnectionErrorRetryAttempts() {
        return this.connectionErrorRetryAttempts;
    }

    public void setConnectionErrorRetryAttempts(int connectionErrorRetryAttempts) {
        this.connectionErrorRetryAttempts = connectionErrorRetryAttempts;
    }

    public long getDupCloseCount() {
        return this.dupCloseCount;
    }

    public int getMaxPoolPreparedStatementPerConnectionSize() {
        return this.maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
        if (maxPoolPreparedStatementPerConnectionSize > 0) {
            this.poolPreparedStatements = true;
        } else {
            this.poolPreparedStatements = false;
        }

        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public boolean isSharePreparedStatements() {
        return this.sharePreparedStatements;
    }

    public void setSharePreparedStatements(boolean sharePreparedStatements) {
        this.sharePreparedStatements = sharePreparedStatements;
    }

    public void incrementDupCloseCount() {
        dupCloseCountUpdater.incrementAndGet(this);
    }

    public ValidConnectionChecker getValidConnectionChecker() {
        return this.validConnectionChecker;
    }

    public void setValidConnectionChecker(ValidConnectionChecker validConnectionChecker) {
        this.validConnectionChecker = validConnectionChecker;
    }

    public String getValidConnectionCheckerClassName() {
        return this.validConnectionChecker == null ? null : this.validConnectionChecker.getClass().getName();
    }

    public void setValidConnectionCheckerClassName(String validConnectionCheckerClass) throws Exception {
        Class<?> clazz = Utils.loadClass(validConnectionCheckerClass);
        ValidConnectionChecker validConnectionChecker = null;
        if (clazz != null) {
            validConnectionChecker = (ValidConnectionChecker)clazz.newInstance();
            this.validConnectionChecker = validConnectionChecker;
        } else {
            LOG.error("load validConnectionCheckerClass error : " + validConnectionCheckerClass);
        }

    }

    public String getDbType() {
        return this.dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public void addConnectionProperty(String name, String value) {
        if (!StringUtils.equals(this.connectProperties.getProperty(name), value)) {
            if (this.inited) {
                throw new UnsupportedOperationException();
            } else {
                this.connectProperties.put(name, value);
            }
        }
    }

    public Collection<String> getConnectionInitSqls() {
        Collection<String> result = this.connectionInitSqls;
        return result == null ? Collections.emptyList() : result;
    }

    public void setConnectionInitSqls(Collection<? extends Object> connectionInitSqls) {
        if (connectionInitSqls != null && connectionInitSqls.size() > 0) {
            ArrayList<String> newVal = null;
            Iterator var3 = connectionInitSqls.iterator();

            while(var3.hasNext()) {
                Object o = var3.next();
                if (o != null) {
                    String s = o.toString();
                    s = s.trim();
                    if (s.length() != 0) {
                        if (newVal == null) {
                            newVal = new ArrayList();
                        }

                        newVal.add(s);
                    }
                }
            }

            this.connectionInitSqls = newVal;
        } else {
            this.connectionInitSqls = null;
        }

    }

    public long getTimeBetweenConnectErrorMillis() {
        return this.timeBetweenConnectErrorMillis;
    }

    public void setTimeBetweenConnectErrorMillis(long timeBetweenConnectErrorMillis) {
        this.timeBetweenConnectErrorMillis = timeBetweenConnectErrorMillis;
    }

    public int getMaxOpenPreparedStatements() {
        return this.maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.setMaxPoolPreparedStatementPerConnectionSize(maxOpenPreparedStatements);
    }

    public boolean isLogAbandoned() {
        return this.logAbandoned;
    }

    public void setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public int getRemoveAbandonedTimeout() {
        return (int)(this.removeAbandonedTimeoutMillis / 1000L);
    }

    public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
        this.removeAbandonedTimeoutMillis = (long)removeAbandonedTimeout * 1000L;
    }

    public void setRemoveAbandonedTimeoutMillis(long removeAbandonedTimeoutMillis) {
        this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
    }

    public long getRemoveAbandonedTimeoutMillis() {
        return this.removeAbandonedTimeoutMillis;
    }

    public boolean isRemoveAbandoned() {
        return this.removeAbandoned;
    }

    public void setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public long getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        if (minEvictableIdleTimeMillis < 30000L) {
            LOG.error("minEvictableIdleTimeMillis should be greater than 30000");
        }

        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public long getKeepAliveBetweenTimeMillis() {
        return this.keepAliveBetweenTimeMillis;
    }

    public void setKeepAliveBetweenTimeMillis(long keepAliveBetweenTimeMillis) {
        if (keepAliveBetweenTimeMillis < 30000L) {
            LOG.error("keepAliveBetweenTimeMillis should be greater than 30000");
        }

        this.keepAliveBetweenTimeMillis = keepAliveBetweenTimeMillis;
    }

    public long getMaxEvictableIdleTimeMillis() {
        return this.maxEvictableIdleTimeMillis;
    }

    public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
        if (maxEvictableIdleTimeMillis < 30000L) {
            LOG.error("maxEvictableIdleTimeMillis should be greater than 30000");
        }

        if (maxEvictableIdleTimeMillis < this.minEvictableIdleTimeMillis) {
            throw new IllegalArgumentException("maxEvictableIdleTimeMillis must be grater than minEvictableIdleTimeMillis");
        } else {
            this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
        }
    }

    public long getPhyTimeoutMillis() {
        return this.phyTimeoutMillis;
    }

    public void setPhyTimeoutMillis(long phyTimeoutMillis) {
        this.phyTimeoutMillis = phyTimeoutMillis;
    }

    public long getPhyMaxUseCount() {
        return this.phyMaxUseCount;
    }

    public void setPhyMaxUseCount(long phyMaxUseCount) {
        this.phyMaxUseCount = phyMaxUseCount;
    }

    public int getNumTestsPerEvictionRun() {
        return this.numTestsPerEvictionRun;
    }

    /** @deprecated */
    @Deprecated
    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getMaxWaitThreadCount() {
        return this.maxWaitThreadCount;
    }

    public void setMaxWaitThreadCount(int maxWaithThreadCount) {
        this.maxWaitThreadCount = maxWaithThreadCount;
    }

    public String getValidationQuery() {
        return this.validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public int getValidationQueryTimeout() {
        return this.validationQueryTimeout;
    }

    public void setValidationQueryTimeout(int validationQueryTimeout) {
        if (validationQueryTimeout < 0 && "sqlserver".equals(this.dbType)) {
            LOG.error("validationQueryTimeout should be >= 0");
        }

        this.validationQueryTimeout = validationQueryTimeout;
    }

    public boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }

    public void setAccessToUnderlyingConnectionAllowed(boolean accessToUnderlyingConnectionAllowed) {
        this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
    }

    public boolean isTestOnBorrow() {
        return this.testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return this.testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return this.testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean isDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public Boolean getDefaultReadOnly() {
        return this.defaultReadOnly;
    }

    public void setDefaultReadOnly(Boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public Integer getDefaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }

    public void setDefaultTransactionIsolation(Integer defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }

    public String getDefaultCatalog() {
        return this.defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public PasswordCallback getPasswordCallback() {
        return this.passwordCallback;
    }

    public void setPasswordCallback(PasswordCallback passwordCallback) {
        this.passwordCallback = passwordCallback;
    }

    public void setPasswordCallbackClassName(String passwordCallbackClassName) throws Exception {
        Class<?> clazz = Utils.loadClass(passwordCallbackClassName);
        if (clazz != null) {
            this.passwordCallback = (PasswordCallback)clazz.newInstance();
        } else {
            LOG.error("load passwordCallback error : " + passwordCallbackClassName);
            this.passwordCallback = null;
        }

    }

    public NameCallback getUserCallback() {
        return this.userCallback;
    }

    public void setUserCallback(NameCallback userCallback) {
        this.userCallback = userCallback;
    }

    public boolean isInitVariants() {
        return this.initVariants;
    }

    public void setInitVariants(boolean initVariants) {
        this.initVariants = initVariants;
    }

    public boolean isInitGlobalVariants() {
        return this.initGlobalVariants;
    }

    public void setInitGlobalVariants(boolean initGlobalVariants) {
        this.initGlobalVariants = initGlobalVariants;
    }

    public int getQueryTimeout() {
        return this.queryTimeout;
    }

    public void setQueryTimeout(int seconds) {
        this.queryTimeout = seconds;
    }

    public String getName() {
        return this.name != null ? this.name : "DataSource-" + System.identityHashCode(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPoolPreparedStatements() {
        return this.poolPreparedStatements;
    }

    public abstract void setPoolPreparedStatements(boolean var1);

    public long getMaxWait() {
        return this.maxWait;
    }

    public void setMaxWait(long maxWaitMillis) {
        if (maxWaitMillis != this.maxWait) {
            if (maxWaitMillis > 0L && this.useUnfairLock == null && !this.inited) {
                ReentrantLock lock = this.lock;
                lock.lock();

                try {
                    if (!this.inited && !lock.isFair()) {
                        this.lock = new ReentrantLock(true);
                        this.notEmpty = this.lock.newCondition();
                        this.empty = this.lock.newCondition();
                    }
                } finally {
                    lock.unlock();
                }
            }

            if (this.inited) {
                LOG.error("maxWait changed : " + this.maxWait + " -> " + maxWaitMillis);
            }

            this.maxWait = maxWaitMillis;
        }
    }

    public int getNotFullTimeoutRetryCount() {
        return this.notFullTimeoutRetryCount;
    }

    public void setNotFullTimeoutRetryCount(int notFullTimeoutRetryCount) {
        this.notFullTimeoutRetryCount = notFullTimeoutRetryCount;
    }

    public int getMinIdle() {
        return this.minIdle;
    }

    public void setMinIdle(int value) {
        if (value != this.minIdle) {
            if (this.inited && value > this.maxActive) {
                throw new IllegalArgumentException("minIdle greater than maxActive, " + this.maxActive + " < " + this.minIdle);
            } else if (this.minIdle < 0) {
                throw new IllegalArgumentException("minIdle must > 0");
            } else {
                this.minIdle = value;
            }
        }
    }

    public int getMaxIdle() {
        return this.maxIdle;
    }

    /** @deprecated */
    @Deprecated
    public void setMaxIdle(int maxIdle) {
        LOG.error("maxIdle is deprecated");
        this.maxIdle = maxIdle;
    }

    public int getInitialSize() {
        return this.initialSize;
    }

    public void setInitialSize(int initialSize) {
        if (this.initialSize != initialSize) {
            if (this.inited) {
                throw new UnsupportedOperationException();
            } else {
                this.initialSize = initialSize;
            }
        }
    }

    public long getCreateErrorCount() {
        return (long)this.createErrorCount;
    }

    public int getMaxActive() {
        return this.maxActive;
    }

    public abstract void setMaxActive(int var1);

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        if (!StringUtils.equals(this.username, username)) {
//            if (this.inited) {
//                throw new UnsupportedOperationException();
//            } else {
                this.username = username;
//            }
        }
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        if (!StringUtils.equals(this.password, password)) {
            if (this.inited) {
                LOG.info("password changed");
            }

            this.password = password;
        }
    }

    public Properties getConnectProperties() {
        return this.connectProperties;
    }

    public abstract void setConnectProperties(Properties var1);

    public void setConnectionProperties(String connectionProperties) {
        if (connectionProperties != null && connectionProperties.trim().length() != 0) {
            String[] entries = connectionProperties.split(";");
            Properties properties = new Properties();

            for(int i = 0; i < entries.length; ++i) {
                String entry = entries[i];
                if (entry.length() > 0) {
                    int index = entry.indexOf(61);
                    if (index > 0) {
                        String name = entry.substring(0, index);
                        String value = entry.substring(index + 1);
                        properties.setProperty(name, value);
                    } else {
                        properties.setProperty(entry, "");
                    }
                }
            }

            this.setConnectProperties(properties);
        } else {
            this.setConnectProperties((Properties)null);
        }
    }

    public String getUrl() {
        return this.jdbcUrl;
    }

    public String getRawJdbcUrl() {
        return this.jdbcUrl;
    }

    public void setUrl(String jdbcUrl) {
        if (!StringUtils.equals(this.jdbcUrl, jdbcUrl)) {
//            if (this.inited) {
//                throw new UnsupportedOperationException();
//            } else {
                if (jdbcUrl != null) {
                    jdbcUrl = jdbcUrl.trim();
                }

                this.jdbcUrl = jdbcUrl;
//            }
        }
    }

    public String getDriverClassName() {
        return this.driverClass;
    }

    public void setDriverClassName(String driverClass) {
        if (driverClass != null && driverClass.length() > 256) {
            throw new IllegalArgumentException("driverClassName length > 256.");
        } else {
            if ("oracle.jdbc.driver.OracleDriver".equalsIgnoreCase(driverClass)) {
                driverClass = "oracle.jdbc.OracleDriver";
                LOG.warn("oracle.jdbc.driver.OracleDriver is deprecated.Having use oracle.jdbc.OracleDriver.");
            }

            if (this.inited) {
                if (!StringUtils.equals(this.driverClass, driverClass)) {
                    throw new UnsupportedOperationException();
                }
            } else {
                this.driverClass = driverClass;
            }
        }
    }

    public ClassLoader getDriverClassLoader() {
        return this.driverClassLoader;
    }

    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;
    }

    public PrintWriter getLogWriter() {
        return this.logWriter;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    public void setLoginTimeout(int seconds) {
        DriverManager.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    public Driver getDriver() {
        return this.driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public int getDriverMajorVersion() {
        return this.driver == null ? -1 : this.driver.getMajorVersion();
    }

    public int getDriverMinorVersion() {
        return this.driver == null ? -1 : this.driver.getMinorVersion();
    }

    public ExceptionSorter getExceptionSorter() {
        return this.exceptionSorter;
    }

    public String getExceptionSorterClassName() {
        return this.exceptionSorter == null ? null : this.exceptionSorter.getClass().getName();
    }

    public void setExceptionSorter(ExceptionSorter exceptionSoter) {
        this.exceptionSorter = exceptionSoter;
    }

    public void setExceptionSorterClassName(String exceptionSorter) throws Exception {
        this.setExceptionSorter(exceptionSorter);
    }

    public void setExceptionSorter(String exceptionSorter) throws SQLException {
        if (exceptionSorter == null) {
            this.exceptionSorter = NullExceptionSorter.getInstance();
        } else {
            exceptionSorter = exceptionSorter.trim();
            if (exceptionSorter.length() == 0) {
                this.exceptionSorter = NullExceptionSorter.getInstance();
            } else {
                Class<?> clazz = Utils.loadClass(exceptionSorter);
                if (clazz == null) {
                    LOG.error("load exceptionSorter error : " + exceptionSorter);
                } else {
                    try {
                        this.exceptionSorter = (ExceptionSorter)clazz.newInstance();
                    } catch (Exception var4) {
                        throw new SQLException("create exceptionSorter error", var4);
                    }
                }

            }
        }
    }

    public List<Filter> getProxyFilters() {
        return this.filters;
    }

    public void setProxyFilters(List<Filter> filters) {
        if (filters != null) {
            this.filters.addAll(filters);
        }

    }

    public String[] getFilterClasses() {
        List<Filter> filterConfigList = this.getProxyFilters();
        List<String> classes = new ArrayList();
        Iterator var3 = filterConfigList.iterator();

        while(var3.hasNext()) {
            Filter filter = (Filter)var3.next();
            classes.add(filter.getClass().getName());
        }

        return (String[])classes.toArray(new String[classes.size()]);
    }

    public void setFilters(String filters) throws SQLException {
        if (filters != null && filters.startsWith("!")) {
            filters = filters.substring(1);
            this.clearFilters();
        }

        this.addFilters(filters);
    }

    public void addFilters(String filters) throws SQLException {
        if (filters != null && filters.length() != 0) {
            String[] filterArray = filters.split("\\,");
            String[] var3 = filterArray;
            int var4 = filterArray.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String item = var3[var5];
                FilterManager.loadFilter(this.filters, item.trim());
            }

        }
    }

    public void clearFilters() {
        if (this.isClearFiltersEnable()) {
            this.filters.clear();
        }
    }

    public void validateConnection(Connection conn) throws SQLException {
        String query = this.getValidationQuery();
        if (conn.isClosed()) {
            throw new SQLException("validateConnection: connection closed");
        } else if (this.validConnectionChecker != null) {
            boolean result = true;
            Exception error = null;

            try {
                result = this.validConnectionChecker.isValidConnection(conn, this.validationQuery, this.validationQueryTimeout);
                if (result && this.onFatalError) {
                    this.lock.lock();

                    try {
                        if (this.onFatalError) {
                            this.onFatalError = false;
                        }
                    } finally {
                        this.lock.unlock();
                    }
                }
            } catch (SQLException var24) {
                throw var24;
            } catch (Exception var25) {
                error = var25;
            }

            if (!result) {
                SQLException sqlError = error != null ? new SQLException("validateConnection false", error) : new SQLException("validateConnection false");
                throw sqlError;
            }
        } else {
            if (null != query) {
                Statement stmt = null;
                ResultSet rs = null;

                try {
                    stmt = conn.createStatement();
                    if (this.getValidationQueryTimeout() > 0) {
                        stmt.setQueryTimeout(this.getValidationQueryTimeout());
                    }

                    rs = stmt.executeQuery(query);
                    if (!rs.next()) {
                        throw new SQLException("validationQuery didn't return a row");
                    }

                    if (this.onFatalError) {
                        this.lock.lock();

                        try {
                            if (this.onFatalError) {
                                this.onFatalError = false;
                            }
                        } finally {
                            this.lock.unlock();
                        }
                    }
                } finally {
                    JdbcUtils.close(rs);
                    JdbcUtils.close(stmt);
                }
            }

        }
    }

    /** @deprecated */
    protected boolean testConnectionInternal(Connection conn) {
        return this.testConnectionInternal((DruidConnectionHolder)null, conn);
    }

    protected boolean testConnectionInternal(DruidConnectionHolder holder, Connection conn) {
        String sqlFile = JdbcSqlStat.getContextSqlFile();
        String sqlName = JdbcSqlStat.getContextSqlName();
        if (sqlFile != null) {
            JdbcSqlStat.setContextSqlFile((String)null);
        }

        if (sqlName != null) {
            JdbcSqlStat.setContextSqlName((String)null);
        }

        try {
            boolean valid;
            if (this.validConnectionChecker == null) {
                if (conn.isClosed()) {
                    valid = false;
                    return valid;
                } else if (null == this.validationQuery) {
                    valid = true;
                    return valid;
                } else {
                    Statement stmt = null;
                    ResultSet rset = null;

                    boolean var7;
                    try {
                        stmt = conn.createStatement();
                        if (this.getValidationQueryTimeout() > 0) {
                            stmt.setQueryTimeout(this.validationQueryTimeout);
                        }

                        rset = stmt.executeQuery(this.validationQuery);
                        if (!rset.next()) {
                            var7 = false;
                            return var7;
                        }
                    } finally {
                        JdbcUtils.close(rset);
                        JdbcUtils.close(stmt);
                    }

                    if (this.onFatalError) {
                        this.lock.lock();

                        try {
                            if (this.onFatalError) {
                                this.onFatalError = false;
                            }
                        } finally {
                            this.lock.unlock();
                        }
                    }

                    var7 = true;
                    return var7;
                }
            } else {
                valid = this.validConnectionChecker.isValidConnection(conn, this.validationQuery, this.validationQueryTimeout);
                long currentTimeMillis = System.currentTimeMillis();
                if (holder != null) {
                    holder.lastValidTimeMillis = currentTimeMillis;
                }

                if (valid && this.isMySql) {
                    long lastPacketReceivedTimeMs = MySqlUtils.getLastPacketReceivedTimeMs(conn);
                    if (lastPacketReceivedTimeMs > 0L) {
                        long mysqlIdleMillis = currentTimeMillis - lastPacketReceivedTimeMs;
                        if (lastPacketReceivedTimeMs > 0L && mysqlIdleMillis >= this.timeBetweenEvictionRunsMillis) {
                            this.discardConnection(conn);
                            String errorMsg = "discard long time none received connection. , jdbcUrl : " + this.jdbcUrl + ", jdbcUrl : " + this.jdbcUrl + ", lastPacketReceivedIdleMillis : " + mysqlIdleMillis;
                            LOG.error(errorMsg);
                            boolean var13 = false;
                            return var13;
                        }
                    }
                }

                if (valid && this.onFatalError) {
                    this.lock.lock();

                    try {
                        if (this.onFatalError) {
                            this.onFatalError = false;
                        }
                    } finally {
                        this.lock.unlock();
                    }
                }

                boolean var46 = valid;
                return var46;
            }
        } catch (Throwable var41) {
            boolean var6 = false;
            return var6;
        } finally {
            if (sqlFile != null) {
                JdbcSqlStat.setContextSqlFile(sqlFile);
            }

            if (sqlName != null) {
                JdbcSqlStat.setContextSqlName(sqlName);
            }

        }
    }

    public Set<DruidPooledConnection> getActiveConnections() {
        this.activeConnectionLock.lock();

        HashSet var1;
        try {
            var1 = new HashSet(this.activeConnections.keySet());
        } finally {
            this.activeConnectionLock.unlock();
        }

        return var1;
    }

    public List<String> getActiveConnectionStackTrace() {
        List<String> list = new ArrayList();
        Iterator var2 = this.getActiveConnections().iterator();

        while(var2.hasNext()) {
            DruidPooledConnection conn = (DruidPooledConnection)var2.next();
            list.add(Utils.toString(conn.getConnectStackTrace()));
        }

        return list;
    }

    public long getCreateTimespanNano() {
        return this.createTimespan;
    }

    public long getCreateTimespanMillis() {
        return this.createTimespan / 1000000L;
    }

    public Driver getRawDriver() {
        return this.driver;
    }

    public boolean isClearFiltersEnable() {
        return this.clearFiltersEnable;
    }

    public void setClearFiltersEnable(boolean clearFiltersEnable) {
        this.clearFiltersEnable = clearFiltersEnable;
    }

    public long createConnectionId() {
        return connectionIdSeedUpdater.incrementAndGet(this);
    }

    public long createStatementId() {
        return statementIdSeedUpdater.getAndIncrement(this);
    }

    public long createMetaDataId() {
        return metaDataIdSeedUpdater.getAndIncrement(this);
    }

    public long createResultSetId() {
        return resultSetIdSeedUpdater.getAndIncrement(this);
    }

    public long createTransactionId() {
        return transactionIdSeedUpdater.getAndIncrement(this);
    }

    void initStatement(DruidPooledConnection conn, Statement stmt) throws SQLException {
        boolean transaction = !conn.getConnectionHolder().underlyingAutoCommit;
        int queryTimeout = transaction ? this.getTransactionQueryTimeout() : this.getQueryTimeout();
        if (queryTimeout > 0) {
            stmt.setQueryTimeout(queryTimeout);
        }

    }

    public void handleConnectionException(DruidPooledConnection conn, Throwable t) throws SQLException {
        this.handleConnectionException(conn, t, (String)null);
    }

    public abstract void handleConnectionException(DruidPooledConnection var1, Throwable var2, String var3) throws SQLException;

    protected abstract void recycle(DruidPooledConnection var1) throws SQLException;

    public Connection createPhysicalConnection(String url, Properties info) throws SQLException {
        Object conn;
        if (this.getProxyFilters().size() == 0) {
            conn = this.getDriver().connect(url, info);
        } else {
            conn = (new FilterChainImpl(this)).connection_connect(info);
        }

        createCountUpdater.incrementAndGet(this);
        return (Connection)conn;
    }

    public DruidAbstractDataSource.PhysicalConnectionInfo createPhysicalConnection() throws SQLException {
        String url = this.getUrl();
        Properties connectProperties = this.getConnectProperties();
        String user;
        if (this.getUserCallback() != null) {
            user = this.getUserCallback().getName();
        } else {
            user = this.getUsername();
        }

        String password = this.getPassword();
        PasswordCallback passwordCallback = this.getPasswordCallback();
        if (passwordCallback != null) {
            if (passwordCallback instanceof DruidPasswordCallback) {
                DruidPasswordCallback druidPasswordCallback = (DruidPasswordCallback)passwordCallback;
                druidPasswordCallback.setUrl(url);
                druidPasswordCallback.setProperties(connectProperties);
            }

            char[] chars = passwordCallback.getPassword();
            if (chars != null) {
                password = new String(chars);
            }
        }

        Properties physicalConnectProperties = new Properties();
        if (connectProperties != null) {
            physicalConnectProperties.putAll(connectProperties);
        }

        if (user != null && user.length() != 0) {
            physicalConnectProperties.put("user", user);
        }

        if (password != null && password.length() != 0) {
            physicalConnectProperties.put("password", password);
        }

        Connection conn = null;
        long connectStartNanos = System.nanoTime();
        Map<String, Object> variables = this.initVariants ? new HashMap() : null;
        Map<String, Object> globalVariables = this.initGlobalVariants ? new HashMap() : null;
        createStartNanosUpdater.set(this, connectStartNanos);
        creatingCountUpdater.incrementAndGet(this);
        boolean var27 = false;

        long connectedNanos;
        long initedNanos;
        long validatedNanos;
        try {
            var27 = true;
            conn = this.createPhysicalConnection(url, physicalConnectProperties);
            connectedNanos = System.nanoTime();
            if (conn == null) {
                throw new SQLException("connect error, url " + url + ", driverClass " + this.driverClass);
            }

            this.initPhysicalConnection(conn, variables, globalVariables);
            initedNanos = System.nanoTime();
            this.validateConnection(conn);
            validatedNanos = System.nanoTime();
            this.setFailContinuous(false);
            this.setCreateError((Throwable)null);
            var27 = false;
        } catch (SQLException var28) {
            this.setCreateError(var28);
            JdbcUtils.close(conn);
            throw var28;
        } catch (RuntimeException var29) {
            this.setCreateError(var29);
            JdbcUtils.close(conn);
            throw var29;
        } catch (Error var30) {
            createErrorCountUpdater.incrementAndGet(this);
            this.setCreateError(var30);
            JdbcUtils.close(conn);
            throw var30;
        } finally {
            if (var27) {
                long nano = System.nanoTime() - connectStartNanos;
                this.createTimespan += nano;
                creatingCountUpdater.decrementAndGet(this);
            }
        }

        long nano = System.nanoTime() - connectStartNanos;
        this.createTimespan += nano;
        creatingCountUpdater.decrementAndGet(this);
        return new DruidAbstractDataSource.PhysicalConnectionInfo(conn, connectStartNanos, connectedNanos, initedNanos, validatedNanos, variables, globalVariables);
    }

    protected void setCreateError(Throwable ex) {
        if (ex == null) {
            this.lock.lock();

            try {
                if (this.createError != null) {
                    this.createError = null;
                }
            } finally {
                this.lock.unlock();
            }

        } else {
            createErrorCountUpdater.incrementAndGet(this);
            long now = System.currentTimeMillis();
            this.lock.lock();

            try {
                this.createError = ex;
                this.lastCreateError = ex;
                this.lastCreateErrorTimeMillis = now;
            } finally {
                this.lock.unlock();
            }

        }
    }

    public boolean isFailContinuous() {
        return failContinuousUpdater.get(this) == 1;
    }

    protected void setFailContinuous(boolean fail) {
        if (fail) {
            failContinuousTimeMillisUpdater.set(this, System.currentTimeMillis());
        } else {
            failContinuousTimeMillisUpdater.set(this, 0L);
        }

        boolean currentState = failContinuousUpdater.get(this) == 1;
        if (currentState != fail) {
            if (fail) {
                failContinuousUpdater.set(this, 1);
                if (LOG.isInfoEnabled()) {
                    LOG.info("{dataSource-" + this.getID() + "} failContinuous is true");
                }
            } else {
                failContinuousUpdater.set(this, 0);
                if (LOG.isInfoEnabled()) {
                    LOG.info("{dataSource-" + this.getID() + "} failContinuous is false");
                }
            }

        }
    }

    public void initPhysicalConnection(Connection conn) throws SQLException {
        this.initPhysicalConnection(conn, (Map)null, (Map)null);
    }

    public void initPhysicalConnection(Connection conn, Map<String, Object> variables, Map<String, Object> globalVariables) throws SQLException {
        if (conn.getAutoCommit() != this.defaultAutoCommit) {
            conn.setAutoCommit(this.defaultAutoCommit);
        }

        if (this.defaultReadOnly != null && conn.isReadOnly() != this.defaultReadOnly) {
            conn.setReadOnly(this.defaultReadOnly);
        }

        if (this.getDefaultTransactionIsolation() != null && conn.getTransactionIsolation() != this.getDefaultTransactionIsolation()) {
            conn.setTransactionIsolation(this.getDefaultTransactionIsolation());
        }

        if (this.getDefaultCatalog() != null && this.getDefaultCatalog().length() != 0) {
            conn.setCatalog(this.getDefaultCatalog());
        }

        Collection<String> initSqls = this.getConnectionInitSqls();
        if (initSqls.size() != 0 || variables != null || globalVariables != null) {
            Statement stmt = null;

            try {
                stmt = conn.createStatement();
                Iterator var6 = initSqls.iterator();

                String name;
                while(var6.hasNext()) {
                    name = (String)var6.next();
                    if (name != null) {
                        stmt.execute(name);
                    }
                }

                if ("mysql".equals(this.dbType) || "aliyun_ads".equals(this.dbType)) {
                    ResultSet rs;
                    Object value;
                    if (variables != null) {
                        rs = null;

                        try {
                            rs = stmt.executeQuery("show variables");

                            while(rs.next()) {
                                name = rs.getString(1);
                                value = rs.getObject(2);
                                variables.put(name, value);
                            }
                        } finally {
                            JdbcUtils.close(rs);
                        }
                    }

                    if (globalVariables != null) {
                        rs = null;

                        try {
                            rs = stmt.executeQuery("show global variables");

                            while(rs.next()) {
                                name = rs.getString(1);
                                value = rs.getObject(2);
                                globalVariables.put(name, value);
                            }
                        } finally {
                            JdbcUtils.close(rs);
                        }
                    }
                }
            } finally {
                JdbcUtils.close(stmt);
            }

        }
    }

    public abstract int getActivePeak();

    public CompositeDataSupport getCompositeData() throws JMException {
        JdbcDataSourceStat stat = this.getDataSourceStat();
        Map<String, Object> map = new HashMap();
        map.put("ID", this.getID());
        map.put("URL", this.getUrl());
        map.put("Name", this.getName());
        map.put("FilterClasses", this.getFilterClasses());
        map.put("CreatedTime", this.getCreatedTime());
        map.put("RawDriverClassName", this.getDriverClassName());
        map.put("RawUrl", this.getUrl());
        map.put("RawDriverMajorVersion", this.getRawDriverMajorVersion());
        map.put("RawDriverMinorVersion", this.getRawDriverMinorVersion());
        map.put("Properties", this.getProperties());
        map.put("ConnectionActiveCount", (long)this.getActiveCount());
        map.put("ConnectionActiveCountMax", this.getActivePeak());
        map.put("ConnectionCloseCount", this.getCloseCount());
        map.put("ConnectionCommitCount", this.getCommitCount());
        map.put("ConnectionRollbackCount", this.getRollbackCount());
        map.put("ConnectionConnectLastTime", stat.getConnectionStat().getConnectLastTime());
        map.put("ConnectionConnectErrorCount", this.getCreateCount());
        if (this.createError != null) {
            map.put("ConnectionConnectErrorLastTime", this.getLastCreateErrorTime());
            map.put("ConnectionConnectErrorLastMessage", this.createError.getMessage());
            map.put("ConnectionConnectErrorLastStackTrace", Utils.getStackTrace(this.createError));
        } else {
            map.put("ConnectionConnectErrorLastTime", (Object)null);
            map.put("ConnectionConnectErrorLastMessage", (Object)null);
            map.put("ConnectionConnectErrorLastStackTrace", (Object)null);
        }

        map.put("StatementCreateCount", stat.getStatementStat().getCreateCount());
        map.put("StatementPrepareCount", stat.getStatementStat().getPrepareCount());
        map.put("StatementPreCallCount", stat.getStatementStat().getPrepareCallCount());
        map.put("StatementExecuteCount", stat.getStatementStat().getExecuteCount());
        map.put("StatementRunningCount", stat.getStatementStat().getRunningCount());
        map.put("StatementConcurrentMax", stat.getStatementStat().getConcurrentMax());
        map.put("StatementCloseCount", stat.getStatementStat().getCloseCount());
        map.put("StatementErrorCount", stat.getStatementStat().getErrorCount());
        map.put("StatementLastErrorTime", (Object)null);
        map.put("StatementLastErrorMessage", (Object)null);
        map.put("StatementLastErrorStackTrace", (Object)null);
        map.put("StatementExecuteMillisTotal", stat.getStatementStat().getMillisTotal());
        map.put("StatementExecuteLastTime", stat.getStatementStat().getExecuteLastTime());
        map.put("ConnectionConnectingCount", stat.getConnectionStat().getConnectingCount());
        map.put("ResultSetCloseCount", stat.getResultSetStat().getCloseCount());
        map.put("ResultSetOpenCount", stat.getResultSetStat().getOpenCount());
        map.put("ResultSetOpenningCount", stat.getResultSetStat().getOpeningCount());
        map.put("ResultSetOpenningMax", stat.getResultSetStat().getOpeningMax());
        map.put("ResultSetFetchRowCount", stat.getResultSetStat().getFetchRowCount());
        map.put("ResultSetLastOpenTime", stat.getResultSetStat().getLastOpenTime());
        map.put("ResultSetErrorCount", stat.getResultSetStat().getErrorCount());
        map.put("ResultSetOpenningMillisTotal", stat.getResultSetStat().getAliveMillisTotal());
        map.put("ResultSetLastErrorTime", stat.getResultSetStat().getLastErrorTime());
        map.put("ResultSetLastErrorMessage", (Object)null);
        map.put("ResultSetLastErrorStackTrace", (Object)null);
        map.put("ConnectionConnectCount", this.getConnectCount());
        if (this.createError != null) {
            map.put("ConnectionErrorLastMessage", this.createError.getMessage());
            map.put("ConnectionErrorLastStackTrace", Utils.getStackTrace(this.createError));
        } else {
            map.put("ConnectionErrorLastMessage", (Object)null);
            map.put("ConnectionErrorLastStackTrace", (Object)null);
        }

        map.put("ConnectionConnectMillisTotal", stat.getConnectionStat().getConnectMillis());
        map.put("ConnectionConnectingCountMax", stat.getConnectionStat().getConnectingMax());
        map.put("ConnectionConnectMillisMax", stat.getConnectionStat().getConnectMillisMax());
        map.put("ConnectionErrorLastTime", stat.getConnectionStat().getErrorLastTime());
        map.put("ConnectionAliveMillisMax", stat.getConnectionConnectAliveMillisMax());
        map.put("ConnectionAliveMillisMin", stat.getConnectionConnectAliveMillisMin());
        map.put("ConnectionHistogram", stat.getConnectionHistogramValues());
        map.put("StatementHistogram", stat.getStatementStat().getHistogramValues());
        return new CompositeDataSupport(JdbcStatManager.getDataSourceCompositeType(), map);
    }

    public long getID() {
        return this.id;
    }

    public Date getCreatedTime() {
        return this.createdTime;
    }

    public abstract int getRawDriverMajorVersion();

    public abstract int getRawDriverMinorVersion();

    public abstract String getProperties();

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    public void closePreapredStatement(PreparedStatementHolder stmtHolder) {
        if (stmtHolder != null) {
            closedPreparedStatementCountUpdater.incrementAndGet(this);
            this.decrementCachedPreparedStatementCount();
            this.incrementCachedPreparedStatementDeleteCount();
            JdbcUtils.close(stmtHolder.statement);
        }
    }

    protected void cloneTo(DruidAbstractDataSource to) {
        to.defaultAutoCommit = this.defaultAutoCommit;
        to.defaultReadOnly = this.defaultReadOnly;
        to.defaultTransactionIsolation = this.defaultTransactionIsolation;
        to.defaultCatalog = this.defaultCatalog;
        to.name = this.name;
        to.username = this.username;
        to.password = this.password;
        to.jdbcUrl = this.jdbcUrl;
        to.driverClass = this.driverClass;
        to.connectProperties = this.connectProperties;
        to.passwordCallback = this.passwordCallback;
        to.userCallback = this.userCallback;
        to.initialSize = this.initialSize;
        to.maxActive = this.maxActive;
        to.minIdle = this.minIdle;
        to.maxIdle = this.maxIdle;
        to.maxWait = this.maxWait;
        to.validationQuery = this.validationQuery;
        to.validationQueryTimeout = this.validationQueryTimeout;
        to.testOnBorrow = this.testOnBorrow;
        to.testOnReturn = this.testOnReturn;
        to.testWhileIdle = this.testWhileIdle;
        to.poolPreparedStatements = this.poolPreparedStatements;
        to.sharePreparedStatements = this.sharePreparedStatements;
        to.maxPoolPreparedStatementPerConnectionSize = this.maxPoolPreparedStatementPerConnectionSize;
        to.logWriter = this.logWriter;
        if (this.filters != null) {
            to.filters = new ArrayList(this.filters);
        }

        to.exceptionSorter = this.exceptionSorter;
        to.driver = this.driver;
        to.queryTimeout = this.queryTimeout;
        to.transactionQueryTimeout = this.transactionQueryTimeout;
        to.accessToUnderlyingConnectionAllowed = this.accessToUnderlyingConnectionAllowed;
        to.timeBetweenEvictionRunsMillis = this.timeBetweenEvictionRunsMillis;
        to.numTestsPerEvictionRun = this.numTestsPerEvictionRun;
        to.minEvictableIdleTimeMillis = this.minEvictableIdleTimeMillis;
        to.removeAbandoned = this.removeAbandoned;
        to.removeAbandonedTimeoutMillis = this.removeAbandonedTimeoutMillis;
        to.logAbandoned = this.logAbandoned;
        to.maxOpenPreparedStatements = this.maxOpenPreparedStatements;
        if (this.connectionInitSqls != null) {
            to.connectionInitSqls = new ArrayList(this.connectionInitSqls);
        }

        to.dbType = this.dbType;
        to.timeBetweenConnectErrorMillis = this.timeBetweenConnectErrorMillis;
        to.validConnectionChecker = this.validConnectionChecker;
        to.connectionErrorRetryAttempts = this.connectionErrorRetryAttempts;
        to.breakAfterAcquireFailure = this.breakAfterAcquireFailure;
        to.transactionThresholdMillis = this.transactionThresholdMillis;
        to.dupCloseLogEnable = this.dupCloseLogEnable;
        to.isOracle = this.isOracle;
        to.useOracleImplicitCache = this.useOracleImplicitCache;
        to.asyncCloseConnectionEnable = this.asyncCloseConnectionEnable;
        to.createScheduler = this.createScheduler;
        to.destroyScheduler = this.destroyScheduler;
    }

    public abstract void discardConnection(Connection var1);

    public boolean isAsyncCloseConnectionEnable() {
        return this.isRemoveAbandoned() ? true : this.asyncCloseConnectionEnable;
    }

    public void setAsyncCloseConnectionEnable(boolean asyncCloseConnectionEnable) {
        this.asyncCloseConnectionEnable = asyncCloseConnectionEnable;
    }

    public ScheduledExecutorService getCreateScheduler() {
        return this.createScheduler;
    }

    public void setCreateScheduler(ScheduledExecutorService createScheduler) {
        if (this.isInited()) {
            throw new DruidRuntimeException("dataSource inited.");
        } else {
            this.createScheduler = createScheduler;
        }
    }

    public ScheduledExecutorService getDestroyScheduler() {
        return this.destroyScheduler;
    }

    public void setDestroyScheduler(ScheduledExecutorService destroyScheduler) {
        if (this.isInited()) {
            throw new DruidRuntimeException("dataSource inited.");
        } else {
            this.destroyScheduler = destroyScheduler;
        }
    }

    public boolean isInited() {
        return this.inited;
    }

    public int getMaxCreateTaskCount() {
        return this.maxCreateTaskCount;
    }

    public void setMaxCreateTaskCount(int maxCreateTaskCount) {
        if (maxCreateTaskCount < 1) {
            throw new IllegalArgumentException();
        } else {
            this.maxCreateTaskCount = maxCreateTaskCount;
        }
    }

    public boolean isFailFast() {
        return this.failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public int getOnFatalErrorMaxActive() {
        return this.onFatalErrorMaxActive;
    }

    public void setOnFatalErrorMaxActive(int onFatalErrorMaxActive) {
        this.onFatalErrorMaxActive = onFatalErrorMaxActive;
    }

    public boolean isOnFatalError() {
        return this.onFatalError;
    }

    public boolean isInitExceptionThrow() {
        return this.initExceptionThrow;
    }

    public void setInitExceptionThrow(boolean initExceptionThrow) {
        this.initExceptionThrow = initExceptionThrow;
    }

    public static class PhysicalConnectionInfo {
        private Connection connection;
        private long connectStartNanos;
        private long connectedNanos;
        private long initedNanos;
        private long validatedNanos;
        private Map<String, Object> vairiables;
        private Map<String, Object> globalVairiables;
        long createTaskId;

        public PhysicalConnectionInfo(Connection connection, long connectStartNanos, long connectedNanos, long initedNanos, long validatedNanos) {
            this(connection, connectStartNanos, connectedNanos, initedNanos, validatedNanos, (Map)null, (Map)null);
        }

        public PhysicalConnectionInfo(Connection connection, long connectStartNanos, long connectedNanos, long initedNanos, long validatedNanos, Map<String, Object> vairiables, Map<String, Object> globalVairiables) {
            this.connection = connection;
            this.connectStartNanos = connectStartNanos;
            this.connectedNanos = connectedNanos;
            this.initedNanos = initedNanos;
            this.validatedNanos = validatedNanos;
            this.vairiables = vairiables;
            this.globalVairiables = globalVairiables;
        }

        public Connection getPhysicalConnection() {
            return this.connection;
        }

        public long getConnectStartNanos() {
            return this.connectStartNanos;
        }

        public long getConnectedNanos() {
            return this.connectedNanos;
        }

        public long getInitedNanos() {
            return this.initedNanos;
        }

        public long getValidatedNanos() {
            return this.validatedNanos;
        }

        public long getConnectNanoSpan() {
            return this.connectedNanos - this.connectStartNanos;
        }

        public Map<String, Object> getVairiables() {
            return this.vairiables;
        }

        public Map<String, Object> getGlobalVairiables() {
            return this.globalVairiables;
        }
    }
}
