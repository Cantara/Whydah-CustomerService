package net.whydah.crmservice.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

@Singleton
public class GetCustomerHandler implements Handler {

    private final CustomerRepository userRepository;

    @Inject
    public GetCustomerHandler(CustomerRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public void handle(Context ctx) throws Exception {

        String userId = ctx.getPathTokens().get("id");

        Blocking.get(() -> userRepository.getCustomer(userId)).then(user -> {
            if (user != null) {
                ctx.render(json(user));
            } else {
                ctx.clientError(404); //Not found
            }
        });
    }
}
