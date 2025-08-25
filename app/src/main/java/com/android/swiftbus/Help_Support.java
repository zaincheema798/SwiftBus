package com.android.swiftbus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class Help_Support extends AppCompatActivity {

    private RecyclerView faqRecyclerView;
    private FAQAdapter faqAdapter;
    private List<FAQ> faqList;
    private List<FAQ> filteredFaqList;
    private TextInputEditText searchEditText;
    private MaterialButton emailSupportButton;
    private MaterialCardView liveChatCard, callSupportCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        initViews();
        setupFAQData();
        setupRecyclerView();
        setupSearchFunctionality();
        setupEmailSupport();


        callSupportCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // call this number
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+923107898620"));
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        faqRecyclerView = findViewById(R.id.faqRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        emailSupportButton = findViewById(R.id.emailSupportButton);
        callSupportCard = findViewById(R.id.callSupportCard);
    }

    private void setupFAQData() {
        faqList = new ArrayList<>();

        // Booking & Reservations
        faqList.add(new FAQ("How do I book a bus ticket?",
                "You can book a ticket through our mobile app or website. Select your departure and arrival cities, choose your travel date, pick your preferred bus and seat, then proceed to payment."));

        faqList.add(new FAQ("Can I book tickets for someone else?",
                "Yes, you can book tickets for family members or friends. Just enter their details during the booking process instead of your own."));

        faqList.add(new FAQ("How far in advance can I book tickets?",
                "You can book tickets up to 30 days in advance of your travel date."));

        faqList.add(new FAQ("Can I select my seat?",
                "Yes, our seat selection feature allows you to choose your preferred seat during booking for a small additional fee."));

        faqList.add(new FAQ("What happens if the bus is full?",
                "If your preferred bus is full, we'll show you alternative departure times and dates with available seats."));

        // Cancellation & Modifications
        faqList.add(new FAQ("How can I cancel my booking?",
                "You can cancel your booking through the app under 'My Bookings' section or by calling our customer service. Cancellation charges may apply based on timing."));

        faqList.add(new FAQ("Can I modify my booking date?",
                "Yes, you can modify your booking up to 2 hours before departure, subject to availability and fare difference."));

        faqList.add(new FAQ("What is your cancellation policy?",
                "Free cancellation up to 24 hours before departure. 50% refund for cancellations 6-24 hours before, and 25% refund for cancellations 2-6 hours before departure."));

        faqList.add(new FAQ("How do I get a refund?",
                "Refunds are processed automatically to your original payment method within 5-7 business days after cancellation."));

        // Payment & Refunds
        faqList.add(new FAQ("What payment methods do you accept?",
                "We accept all major credit cards, debit cards, PayPal, Apple Pay, Google Pay, and bank transfers."));

        faqList.add(new FAQ("Is it safe to pay online?",
                "Yes, we use 256-bit SSL encryption and comply with PCI DSS standards to ensure your payment information is secure."));

        faqList.add(new FAQ("Can I pay cash at the station?",
                "Cash payments are accepted at select stations. However, we recommend online booking to guarantee your seat."));

        faqList.add(new FAQ("Why was my payment declined?",
                "Payment may be declined due to insufficient funds, incorrect card details, or bank restrictions. Please verify your information and try again."));

        faqList.add(new FAQ("How long do refunds take?",
                "Refunds typically take 5-7 business days to appear in your account, depending on your payment method and bank."));

        // Schedules & Routes
        faqList.add(new FAQ("How do I check bus schedules?",
                "Use our app or website to view real-time schedules. Enter your departure and arrival cities to see all available buses."));

        faqList.add(new FAQ("Do you operate on holidays?",
                "Yes, we operate on most holidays with modified schedules. Check our holiday schedule section for specific dates."));

        faqList.add(new FAQ("What if my bus is delayed?",
                "You'll receive real-time notifications about delays. We provide compensation or alternative arrangements for significant delays."));

        faqList.add(new FAQ("How early should I arrive at the station?",
                "Please arrive at least 30 minutes before departure for domestic routes and 60 minutes for interstate routes."));

        faqList.add(new FAQ("Can I track my bus in real-time?",
                "Yes, use the 'Track My Bus' feature in the app to see your bus location and estimated arrival time."));

        // App & Technical Issues
        faqList.add(new FAQ("The app is not working properly, what should I do?",
                "Try restarting the app, checking your internet connection, or updating to the latest version. Contact support if issues persist."));

        faqList.add(new FAQ("I forgot my login password",
                "Use the 'Forgot Password' link on the login screen. We'll send a reset link to your registered email address."));

        faqList.add(new FAQ("How do I update my profile information?",
                "Go to 'My Profile' in the app menu to update your personal information, contact details, and preferences."));

        faqList.add(new FAQ("Can I use the app offline?",
                "Some features like viewing saved tickets work offline, but booking and real-time tracking require an internet connection."));

        faqList.add(new FAQ("Why can't I download my ticket?",
                "Ensure you have a stable internet connection and sufficient storage space. Try logging out and back in if the issue persists."));

        // Travel & Services
        faqList.add(new FAQ("What amenities are available on the bus?",
                "Our buses feature comfortable seating, air conditioning, WiFi, charging ports, and restrooms on longer routes."));

        faqList.add(new FAQ("Can I bring luggage?",
                "Yes, each passenger is allowed one carry-on bag and one checked bag up to 50 lbs. Additional baggage fees apply for extra items."));

        faqList.add(new FAQ("Are pets allowed on the bus?",
                "Service animals are always welcome. Small pets in carriers may be allowed on certain routes with advance notice and additional fees."));

        faqList.add(new FAQ("Is food available on the bus?",
                "Light snacks and beverages are available for purchase on longer routes. You're also welcome to bring your own food."));

        faqList.add(new FAQ("What if I miss my bus?",
                "If you miss your bus, contact customer service immediately. We may be able to transfer you to the next available bus for a small fee."));

        faqList.add(new FAQ("Do you provide student or senior discounts?",
                "Yes, we offer discounts for students (with valid ID) and seniors (65+). Discounts are applied automatically during booking."));

        filteredFaqList = new ArrayList<>(faqList);
    }

    private void setupRecyclerView() {
        faqAdapter = new FAQAdapter(filteredFaqList);
        faqRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        faqRecyclerView.setAdapter(faqAdapter);
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFAQs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFAQs(String query) {
        filteredFaqList.clear();

        if (query.isEmpty()) {
            filteredFaqList.addAll(faqList);
        } else {
            String lowercaseQuery = query.toLowerCase().trim();
            for (FAQ faq : faqList) {
                if (faq.getQuestion().toLowerCase().contains(lowercaseQuery) ||
                        faq.getAnswer().toLowerCase().contains(lowercaseQuery)) {
                    filteredFaqList.add(faq);
                }
            }
        }

        faqAdapter.notifyDataSetChanged();

        if (filteredFaqList.isEmpty() && !query.isEmpty()) {
            Toast.makeText(this, "No matching questions found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupEmailSupport() {
        emailSupportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailSupport();
            }
        });
    }

    private void sendEmailSupport() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:zainalicheema798+SwiftBus@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Swift Bus Support Request");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Swift Bus Support Team,\n\nI need assistance with:\n\n[Please describe your issue here]\n\nThank you,\n[Your Name]");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app found. Please send email to support@swiftbus.com", Toast.LENGTH_LONG).show();
        }
    }
}