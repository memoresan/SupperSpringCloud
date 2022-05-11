package flinksql.cus.tableSouce;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChangelogCsvFormatFactory implements DeserializationFormatFactory {

  // define all options statically
  public static final ConfigOption<String> COLUMN_DELIMITER = ConfigOptions.key("column-delimiter")
    .stringType()
    .defaultValue("|");

  @Override
  public String factoryIdentifier() {
    return "changelog-csv";
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    return Collections.emptySet();
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    final Set<ConfigOption<?>> options = new HashSet<>();
    options.add(COLUMN_DELIMITER);
    return options;
  }

  @Override
  public DecodingFormat<DeserializationSchema<RowData>> createDecodingFormat(
      DynamicTableFactory.Context context,
      ReadableConfig formatOptions) {
    //会调用上面的两个方法来判断是否该有的有了
    FactoryUtil.validateFactoryOptions(this, formatOptions);

    // get the validated options
    final String columnDelimiter = formatOptions.get(COLUMN_DELIMITER);

    // create and return the format
    return new ChangelogCsvFormat(columnDelimiter);
  }
}
