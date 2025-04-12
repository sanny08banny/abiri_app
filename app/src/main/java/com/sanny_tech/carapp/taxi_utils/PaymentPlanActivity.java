package com.sanny_tech.carapp.taxi_utils;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.CreateFunSpaceActivity;
import com.sanny_tech.carapp.adapters.OptionItem2Adapter;
import com.sanny_tech.carapp.databinding.ActivityPaymentPlanBinding;
import com.sanny_tech.carapp.entities.OptionItem;
import com.sanny_tech.carapp.entities.SubscriptionPlan;
import com.sanny_tech.carapp.utils.SimCardManager;

import java.util.ArrayList;
import java.util.List;

public class PaymentPlanActivity extends AppCompatActivity implements OptionItem2Adapter.OnItemClickListener {
    private ActivityPaymentPlanBinding binding;
    private OptionItem2Adapter optionItem2Adapter;
    private List<OptionItem> optionItems = new ArrayList<>();
    private OptionItem selectedPlan;
    private String category = "Now";
    private SubscriptionManager subscriptionStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_plan);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        subscriptionStorage = new SubscriptionManager(PaymentPlanActivity.this);
        OptionItem aPlan = new OptionItem("Daily Plan", "Kshs 150",
                "Your first ride will be on us!! ");
        OptionItem bPlan = new OptionItem("Weekly Plan", "Kshs 900",
                "*****Save 150Kshs*******");
        OptionItem cPlan = new OptionItem("Monthly Plan", "Kshs 3100",
                "*****Save 1400Kshs*******");
        optionItems.add(aPlan);
        optionItems.add(bPlan);
        optionItems.add(cPlan);

        optionItem2Adapter = new OptionItem2Adapter(optionItems);
        optionItem2Adapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(optionItem2Adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.categoryRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = findViewById(checkedId);
                String selectedCategory = checkedRadioButton.getText().toString();

                // Perform actions based on selected category
                if (selectedCategory.equals("Now")) {
                    category = "Now";
                } else if (selectedCategory.equals("Later")) {
                    Toast.makeText(PaymentPlanActivity.this,
                            "Payment will be scheduled", Toast.LENGTH_SHORT).show();
                    category = "Later";
                }
            }
        });

        if (!SimCardManager.getPhoneNumber(this).equals("")) {
            binding.numberEdittext.setText(SimCardManager.getPhoneNumber(this));
        }
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPlan != null) {
                    binding.selectPlanLt.setVisibility(View.GONE);
                    binding.confirmPlanLt.setVisibility(View.VISIBLE);
                    Toast.makeText(PaymentPlanActivity.this, selectedPlan.getTitle() +
                            " selected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PaymentPlanActivity.this, "No plan selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.gotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = binding.confirmPlanLt.getVisibility() == View.VISIBLE;
                if (!isExpanded) {
                    onBackPressed();
                }else {
                    binding.selectPlanLt.setVisibility(View.VISIBLE);
                    binding.confirmPlanLt.setVisibility(View.GONE);
                }
            }
        });
        binding.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category.equals("Now")) {
                    if (selectedPlan.getTitle().equals("Daily Plan")) {
                        long expiryDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // 1 day from now
                        subscriptionStorage.saveSubscriptionPlan(
                                new SubscriptionPlan("Daily Plan", "Kshs 150",
                                        "Your first ride will be on us!!", expiryDate));
                    } else if (selectedPlan.getTitle().equals("Weekly Plan")) {
                        long expiryDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000; // 7 days from now
                        subscriptionStorage.saveSubscriptionPlan(new SubscriptionPlan(
                                "Weekly Plan", "Kshs 900",
                                "*****Save 150Kshs*******", expiryDate));
                    } else if (selectedPlan.getTitle().equals("Monthly Plan")) {
                        long expiryDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000;
                        subscriptionStorage.saveSubscriptionPlan(new SubscriptionPlan(
                                "Monthly Plan", "Kshs 3100",
                                "*****Save 1400Kshs*******", expiryDate));
                    }
                    Toast.makeText(PaymentPlanActivity.this, "Saved successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    schedulePayment();
                }
                Toast.makeText(PaymentPlanActivity.this, "Payment will be available soon",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void schedulePayment() {
    }

    @Override
    public void onItemClick(OptionItem item) {
        selectedPlan = item;
        binding.title.setText(item.getTitle());
        binding.miniTitle.setText(item.getMiniTitle());
        binding.title3.setText(item.getTitle3());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        boolean isExpanded = binding.confirmPlanLt.getVisibility() == View.VISIBLE;
        if (isExpanded) {
            binding.selectPlanLt.setVisibility(View.VISIBLE);
            binding.confirmPlanLt.setVisibility(View.GONE);
        }
    }
}