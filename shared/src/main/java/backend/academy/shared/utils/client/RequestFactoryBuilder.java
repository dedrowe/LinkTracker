package backend.academy.shared.utils.client;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class RequestFactoryBuilder {

    private int connectionTimeoutMillis = 200;

    private int readTimeoutMillis = 3000;

    public RequestFactoryBuilder setConnectionTimeout(int connectionTimeoutMillis) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        return this;
    }

    public RequestFactoryBuilder setReadTimeout(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    public ClientHttpRequestFactory build() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeoutMillis);
        factory.setReadTimeout(readTimeoutMillis);
        return factory;
    }
}
