package backend.academy.shared.utils.client;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ReactorClientHttpRequestFactory;

public class RequestFactoryBuilder {

    private int connectionTimeoutMillis = 200;

    private int readTimeoutMillis = 1500;

    public RequestFactoryBuilder setConnectionTimeout(int connectionTimeoutMillis) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        return this;
    }

    public RequestFactoryBuilder setReadTimeout(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    public ClientHttpRequestFactory build() {
        ReactorClientHttpRequestFactory factory = new ReactorClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeoutMillis);
        factory.setReadTimeout(readTimeoutMillis);
        return factory;
    }
}
