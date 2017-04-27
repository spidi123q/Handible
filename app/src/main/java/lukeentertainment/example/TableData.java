package lukeentertainment.example;

import android.provider.BaseColumns;

/**
 * Created by Nakul on 4/2/2017.
 */

public class TableData {
    public TableData()
    {

    }
    public static abstract class TableInfo implements BaseColumns
    {
        public static final String DATABASE_NAME="Handible";
        public static final String TABLE_NAME="ProjectList";
        public static final String TABLE_CONTENT="ContentList";

        public static final String PROJECT_NAME="ProjectName";
        public static final String PROJECT_ID="ProjectId";
        public static final String PROJECT_DATE="ProjectDate";
        public static final String PROJECT_ITEMS="ProjectItems";
        public static final String PROJECT_ICON="ProjectIcon";

        public static final String CONTENT_NAME="ContentName";

    }
}