package SearchWorm.PDFParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class SearchWormElasticSearch {
    Client client;
    Settings settings;

    public SearchWormElasticSearch(Settings clientsettings) {
        settings = clientsettings;
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
    }

    public void addData(String bookTitle, String pageNumber, String content) throws ElasticsearchException, IOException {
        IndexResponse response = client.prepareIndex("searchworm", bookTitle, pageNumber)
                .setSource(jsonBuilder()
                                .startObject()
                                .field("content", content)
                                .field("timeStamp", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()))
                                .endObject()
                )
                .execute()
                .actionGet();
    }

    public void removePage(String bookTitle, String pageNumber) {
        DeleteResponse response = client.prepareDelete("searchworm", bookTitle, pageNumber)
                .execute()
                .actionGet();
    }

    public void removeBook(String bookTitle) {
        DeleteByQueryResponse response = client.prepareDeleteByQuery("searchworm")
                .setQuery(termQuery("_type", bookTitle))
                .execute()
                .actionGet();
    }

    public String searchBooks(String search) {
//                .setQuery(QueryBuilders.queryString(search))             // Query
        SearchResponse response = client.prepareSearch("searchworm")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.queryStringQuery(search))             // Query
                .setExplain(true)
                .execute()
                .actionGet();

//        System.out.println(response);
        return response.toString();
    }

    public void closeClient() {
        client.close();
    }
}
