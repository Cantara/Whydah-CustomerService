package net.whydah.crmservice.customer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.whydah.crmservice.security.Authentication;
import net.whydah.sso.extensions.crmcustomer.types.Customer;
import net.whydah.sso.user.types.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.internal.UuidBasedRequestIdGenerator;

import java.sql.SQLIntegrityConstraintViolationException;

import static ratpack.jackson.Jackson.fromJson;

@Singleton
public class CreateCustomerHandler implements Handler {


    private static final Logger log = LoggerFactory.getLogger(CreateCustomerHandler.class);

    private final CustomerRepository customerRepository;

    @Inject
    public CreateCustomerHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void handle(Context ctx) throws Exception {

        boolean customerRefIsGenerated;
        String customerRef;

        if (ctx.getPathTokens().get("customerRef") != null) {
            customerRef = ctx.getPathTokens().get("customerRef");
            customerRefIsGenerated = false;
        } else {
            customerRef = UuidBasedRequestIdGenerator.INSTANCE.generate(ctx.getRequest()).toString();
            customerRefIsGenerated = true;
        }

        ctx.parse(fromJson(Customer.class)).then(customer -> {
            Blocking.op(() -> {

                if (customerRefIsGenerated) {
                    //Verify userId
                    if (customer.getId() == null || !customer.getId().equals(Authentication.getAuthenticatedUser().getUid())) {
                        log.debug("User {} not authorized to create data for uid {}", Authentication.getAuthenticatedUser().getUid(), customer.getId());
                        ctx.clientError(401);
                    }
                } else {
                    //Verify customerRef
                    if (customerRef == null || !customerRef.equals(Authentication.getAuthenticatedUser().getPersonRef())) {
                        log.debug("User {} with personRef {} not authorized to create data for personRef {}", Authentication.getAuthenticatedUser().getUid(), Authentication.getAuthenticatedUser().getPersonRef(), customerRef);
                        ctx.clientError(401);
                    }
                }

                customerRepository.createCustomer(customerRef, customer);
            }).onError(throwable -> {
                if (throwable instanceof SQLIntegrityConstraintViolationException) {
                    ctx.clientError(400); //Bad request
                } else {
                    throw new RuntimeException(throwable);
                }
            }).then(() -> {
                ctx.redirect(201, customerRef); //Created
            });
        });
    }
}
