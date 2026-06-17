package com.gridraw.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gridraw.app.data.models.Project;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProjectDao_Impl implements ProjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Project> __insertionAdapterOfProject;

  private final EntityDeletionOrUpdateAdapter<Project> __deletionAdapterOfProject;

  private final EntityDeletionOrUpdateAdapter<Project> __updateAdapterOfProject;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProjectById;

  public ProjectDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProject = new EntityInsertionAdapter<Project>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `projects` (`id`,`name`,`createdAt`,`updatedAt`,`thumbnailPath`,`imageUri`,`paperSize`,`orientation`,`customWidthMm`,`customHeightMm`,`gridConfigJson`,`filtersJson`,`cropJson`,`ppi`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Project entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getUpdatedAt());
        if (entity.getThumbnailPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getThumbnailPath());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUri());
        }
        statement.bindString(7, entity.getPaperSize());
        statement.bindString(8, entity.getOrientation());
        statement.bindDouble(9, entity.getCustomWidthMm());
        statement.bindDouble(10, entity.getCustomHeightMm());
        statement.bindString(11, entity.getGridConfigJson());
        statement.bindString(12, entity.getFiltersJson());
        statement.bindString(13, entity.getCropJson());
        statement.bindDouble(14, entity.getPpi());
      }
    };
    this.__deletionAdapterOfProject = new EntityDeletionOrUpdateAdapter<Project>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `projects` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Project entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfProject = new EntityDeletionOrUpdateAdapter<Project>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `projects` SET `id` = ?,`name` = ?,`createdAt` = ?,`updatedAt` = ?,`thumbnailPath` = ?,`imageUri` = ?,`paperSize` = ?,`orientation` = ?,`customWidthMm` = ?,`customHeightMm` = ?,`gridConfigJson` = ?,`filtersJson` = ?,`cropJson` = ?,`ppi` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Project entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getUpdatedAt());
        if (entity.getThumbnailPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getThumbnailPath());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUri());
        }
        statement.bindString(7, entity.getPaperSize());
        statement.bindString(8, entity.getOrientation());
        statement.bindDouble(9, entity.getCustomWidthMm());
        statement.bindDouble(10, entity.getCustomHeightMm());
        statement.bindString(11, entity.getGridConfigJson());
        statement.bindString(12, entity.getFiltersJson());
        statement.bindString(13, entity.getCropJson());
        statement.bindDouble(14, entity.getPpi());
        statement.bindLong(15, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteProjectById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM projects WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProject(final Project project, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfProject.insertAndReturnId(project);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProject(final Project project, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfProject.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProject(final Project project, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProject.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProjectById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProjectById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteProjectById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Project>> getAllProjects() {
    final String _sql = "SELECT * FROM projects ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"projects"}, new Callable<List<Project>>() {
      @Override
      @NonNull
      public List<Project> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPaperSize = CursorUtil.getColumnIndexOrThrow(_cursor, "paperSize");
          final int _cursorIndexOfOrientation = CursorUtil.getColumnIndexOrThrow(_cursor, "orientation");
          final int _cursorIndexOfCustomWidthMm = CursorUtil.getColumnIndexOrThrow(_cursor, "customWidthMm");
          final int _cursorIndexOfCustomHeightMm = CursorUtil.getColumnIndexOrThrow(_cursor, "customHeightMm");
          final int _cursorIndexOfGridConfigJson = CursorUtil.getColumnIndexOrThrow(_cursor, "gridConfigJson");
          final int _cursorIndexOfFiltersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "filtersJson");
          final int _cursorIndexOfCropJson = CursorUtil.getColumnIndexOrThrow(_cursor, "cropJson");
          final int _cursorIndexOfPpi = CursorUtil.getColumnIndexOrThrow(_cursor, "ppi");
          final List<Project> _result = new ArrayList<Project>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Project _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpPaperSize;
            _tmpPaperSize = _cursor.getString(_cursorIndexOfPaperSize);
            final String _tmpOrientation;
            _tmpOrientation = _cursor.getString(_cursorIndexOfOrientation);
            final float _tmpCustomWidthMm;
            _tmpCustomWidthMm = _cursor.getFloat(_cursorIndexOfCustomWidthMm);
            final float _tmpCustomHeightMm;
            _tmpCustomHeightMm = _cursor.getFloat(_cursorIndexOfCustomHeightMm);
            final String _tmpGridConfigJson;
            _tmpGridConfigJson = _cursor.getString(_cursorIndexOfGridConfigJson);
            final String _tmpFiltersJson;
            _tmpFiltersJson = _cursor.getString(_cursorIndexOfFiltersJson);
            final String _tmpCropJson;
            _tmpCropJson = _cursor.getString(_cursorIndexOfCropJson);
            final float _tmpPpi;
            _tmpPpi = _cursor.getFloat(_cursorIndexOfPpi);
            _item = new Project(_tmpId,_tmpName,_tmpCreatedAt,_tmpUpdatedAt,_tmpThumbnailPath,_tmpImageUri,_tmpPaperSize,_tmpOrientation,_tmpCustomWidthMm,_tmpCustomHeightMm,_tmpGridConfigJson,_tmpFiltersJson,_tmpCropJson,_tmpPpi);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getProjectById(final long id, final Continuation<? super Project> $completion) {
    final String _sql = "SELECT * FROM projects WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Project>() {
      @Override
      @Nullable
      public Project call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPaperSize = CursorUtil.getColumnIndexOrThrow(_cursor, "paperSize");
          final int _cursorIndexOfOrientation = CursorUtil.getColumnIndexOrThrow(_cursor, "orientation");
          final int _cursorIndexOfCustomWidthMm = CursorUtil.getColumnIndexOrThrow(_cursor, "customWidthMm");
          final int _cursorIndexOfCustomHeightMm = CursorUtil.getColumnIndexOrThrow(_cursor, "customHeightMm");
          final int _cursorIndexOfGridConfigJson = CursorUtil.getColumnIndexOrThrow(_cursor, "gridConfigJson");
          final int _cursorIndexOfFiltersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "filtersJson");
          final int _cursorIndexOfCropJson = CursorUtil.getColumnIndexOrThrow(_cursor, "cropJson");
          final int _cursorIndexOfPpi = CursorUtil.getColumnIndexOrThrow(_cursor, "ppi");
          final Project _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpPaperSize;
            _tmpPaperSize = _cursor.getString(_cursorIndexOfPaperSize);
            final String _tmpOrientation;
            _tmpOrientation = _cursor.getString(_cursorIndexOfOrientation);
            final float _tmpCustomWidthMm;
            _tmpCustomWidthMm = _cursor.getFloat(_cursorIndexOfCustomWidthMm);
            final float _tmpCustomHeightMm;
            _tmpCustomHeightMm = _cursor.getFloat(_cursorIndexOfCustomHeightMm);
            final String _tmpGridConfigJson;
            _tmpGridConfigJson = _cursor.getString(_cursorIndexOfGridConfigJson);
            final String _tmpFiltersJson;
            _tmpFiltersJson = _cursor.getString(_cursorIndexOfFiltersJson);
            final String _tmpCropJson;
            _tmpCropJson = _cursor.getString(_cursorIndexOfCropJson);
            final float _tmpPpi;
            _tmpPpi = _cursor.getFloat(_cursorIndexOfPpi);
            _result = new Project(_tmpId,_tmpName,_tmpCreatedAt,_tmpUpdatedAt,_tmpThumbnailPath,_tmpImageUri,_tmpPaperSize,_tmpOrientation,_tmpCustomWidthMm,_tmpCustomHeightMm,_tmpGridConfigJson,_tmpFiltersJson,_tmpCropJson,_tmpPpi);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getProjectCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM projects";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
