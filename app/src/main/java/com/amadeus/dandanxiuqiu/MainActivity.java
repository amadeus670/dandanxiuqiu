package com.amadeus.dandanxiuqiu;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int NAVY = Color.rgb(16, 26, 53);
    private static final int BLUE = Color.rgb(65, 105, 225);
    private static final int SURFACE = Color.rgb(245, 247, 252);
    private static final int MUTED = Color.rgb(91, 101, 125);

    private EditText distanceInput;
    private EditText heightInput;
    private EditText windInput;
    private EditText angleInput;
    private EditText windCoefficientInput;
    private EditText heightCoefficientInput;
    private EditText angleCoefficientInput;
    private RadioGroup windDirectionGroup;
    private TextView resultPower;
    private TextView resultRange;
    private TextView resultDetail;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        preferences = getSharedPreferences("calculator", MODE_PRIVATE);
        setContentView(buildContent());
        restoreValues();
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(SURFACE);

        LinearLayout page = vertical();
        page.setPadding(dp(18), dp(22), dp(18), dp(36));
        scroll.addView(page, matchWrap());

        TextView eyebrow = text("DANDAN POWER LAB", 12, Color.rgb(153, 174, 255));
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        eyebrow.setLetterSpacing(0.18f);
        page.addView(eyebrow);

        TextView title = text("65° 力度计算器", 30, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(5), 0, dp(4));

        TextView subtitle = text("输入距离、风力和高度差，立即得到推荐力度", 14,
                Color.rgb(205, 213, 238));
        subtitle.setPadding(0, 0, 0, dp(20));

        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));
        hero.setBackground(rounded(NAVY, 22));
        hero.addView(title);
        hero.addView(subtitle);
        page.addView(hero, fullWidth());

        LinearLayout formCard = card();
        addSectionTitle(formCard, "实战参数", "目标高于自己时，高度差填正数");
        distanceInput = addNumberField(formCard, "水平距离（屏）", "例如 12.6", false);
        heightInput = addNumberField(formCard, "高度差", "例如 -0.8", true);
        windInput = addNumberField(formCard, "风力", "例如 6.1", false);
        angleInput = addNumberField(formCard, "当前角度", "默认 65", false);

        TextView directionLabel = label("风向");
        formCard.addView(directionLabel);
        windDirectionGroup = new RadioGroup(this);
        windDirectionGroup.setOrientation(LinearLayout.HORIZONTAL);
        windDirectionGroup.setPadding(0, dp(4), 0, dp(12));
        addDirectionOption("顺风", 1001);
        addDirectionOption("无风", 1002);
        addDirectionOption("逆风", 1003);
        formCard.addView(windDirectionGroup, fullWidth());

        Button calculate = new Button(this);
        calculate.setText("计算推荐力度");
        calculate.setTextColor(Color.WHITE);
        calculate.setTextSize(17);
        calculate.setTypeface(Typeface.DEFAULT_BOLD);
        calculate.setAllCaps(false);
        calculate.setBackground(rounded(BLUE, 14));
        calculate.setOnClickListener(v -> calculate());
        LinearLayout.LayoutParams buttonParams = fullWidth();
        buttonParams.height = dp(56);
        buttonParams.topMargin = dp(6);
        formCard.addView(calculate, buttonParams);
        page.addView(formCard, cardParams());

        LinearLayout resultCard = vertical();
        resultCard.setPadding(dp(22), dp(20), dp(22), dp(20));
        resultCard.setBackground(rounded(Color.rgb(232, 237, 255), 20));
        TextView resultLabel = text("推荐力度", 13, BLUE);
        resultLabel.setTypeface(Typeface.DEFAULT_BOLD);
        resultPower = text("—", 48, NAVY);
        resultPower.setTypeface(Typeface.DEFAULT_BOLD);
        resultRange = text("输入参数后开始计算", 15, MUTED);
        resultDetail = text("基础表：65°、零风、同高度", 12, MUTED);
        resultDetail.setPadding(0, dp(8), 0, 0);
        resultCard.addView(resultLabel);
        resultCard.addView(resultPower);
        resultCard.addView(resultRange);
        resultCard.addView(resultDetail);
        page.addView(resultCard, cardParams());

        LinearLayout tuningCard = card();
        addSectionTitle(tuningCard, "校准系数", "不同服务器可能有差异；命中数据越多，校准越准");
        windCoefficientInput = addNumberField(tuningCard, "每级风力修正", "默认 0.55", false);
        heightCoefficientInput = addNumberField(tuningCard, "每格高度修正", "默认 1.20", false);
        angleCoefficientInput = addNumberField(tuningCard, "每度角度修正", "默认 0.65", false);
        Button reset = new Button(this);
        reset.setText("恢复默认系数");
        reset.setAllCaps(false);
        reset.setTextColor(BLUE);
        reset.setBackgroundColor(Color.TRANSPARENT);
        reset.setOnClickListener(v -> resetCoefficients());
        tuningCard.addView(reset, fullWidth());
        page.addView(tuningCard, cardParams());

        LinearLayout tableCard = card();
        addSectionTitle(tableCard, "65° 基础力度表", "整数距离之间自动线性插值");
        TextView table = text(
                "距离  1   2   3   4   5   6   7   8   9  10\n" +
                "力度 11  19  26  31  35  39  43  47  50  54\n\n" +
                "距离 11  12  13  14  15  16  17  18  19  20\n" +
                "力度 57  60  63  66  69  72  74  77  80  82",
                13, NAVY);
        table.setTypeface(Typeface.MONOSPACE);
        table.setLineSpacing(0, 1.35f);
        tableCard.addView(table);
        page.addView(tableCard, cardParams());

        TextView disclaimer = text(
                "提示：结果基于游戏说明中的 65° 基础表和可调修正系数，仅供实战参考。建议用实际命中数据持续校准。",
                12, MUTED);
        disclaimer.setLineSpacing(0, 1.25f);
        disclaimer.setPadding(dp(6), dp(4), dp(6), 0);
        page.addView(disclaimer);
        return scroll;
    }

    private void calculate() {
        try {
            double distance = number(distanceInput, "请输入水平距离");
            double height = number(heightInput, "请输入高度差");
            double wind = number(windInput, "请输入风力");
            double angle = number(angleInput, "请输入当前角度");
            double windCoefficient = number(windCoefficientInput, "请输入风力系数");
            double heightCoefficient = number(heightCoefficientInput, "请输入高度系数");
            double angleCoefficient = number(angleCoefficientInput, "请输入角度系数");

            CalculatorEngine.WindDirection direction;
            int selected = windDirectionGroup.getCheckedRadioButtonId();
            if (selected == 1001) direction = CalculatorEngine.WindDirection.TAILWIND;
            else if (selected == 1002) direction = CalculatorEngine.WindDirection.CALM;
            else if (selected == 1003) direction = CalculatorEngine.WindDirection.HEADWIND;
            else throw new IllegalArgumentException("请选择风向");

            CalculatorEngine.Result result = CalculatorEngine.calculate(
                    distance, height, wind, direction, angle,
                    windCoefficient, heightCoefficient, angleCoefficient);
            resultPower.setText(String.valueOf(result.roundedPower));
            resultRange.setText(String.format(Locale.CHINA,
                    "精确值 %.1f  ·  建议范围 %d～%d",
                    result.recommendedPower, result.lowPower, result.highPower));
            resultDetail.setText(result.detailText());

            preferences.edit()
                    .putString("distance", distanceInput.getText().toString())
                    .putString("height", heightInput.getText().toString())
                    .putString("wind", windInput.getText().toString())
                    .putString("angle", angleInput.getText().toString())
                    .putString("windCoefficient", windCoefficientInput.getText().toString())
                    .putString("heightCoefficient", heightCoefficientInput.getText().toString())
                    .putString("angleCoefficient", angleCoefficientInput.getText().toString())
                    .putInt("direction", selected)
                    .apply();
            View focus = getCurrentFocus();
            if (focus != null) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        } catch (IllegalArgumentException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreValues() {
        distanceInput.setText(preferences.getString("distance", "12.6"));
        heightInput.setText(preferences.getString("height", "0"));
        windInput.setText(preferences.getString("wind", "0"));
        angleInput.setText(preferences.getString("angle", "65"));
        windCoefficientInput.setText(preferences.getString("windCoefficient", "0.55"));
        heightCoefficientInput.setText(preferences.getString("heightCoefficient", "1.20"));
        angleCoefficientInput.setText(preferences.getString("angleCoefficient", "0.65"));
        windDirectionGroup.check(preferences.getInt("direction", 1002));
    }

    private void resetCoefficients() {
        windCoefficientInput.setText("0.55");
        heightCoefficientInput.setText("1.20");
        angleCoefficientInput.setText("0.65");
        Toast.makeText(this, "已恢复默认系数", Toast.LENGTH_SHORT).show();
    }

    private void addDirectionOption(String title, int id) {
        RadioButton button = new RadioButton(this);
        button.setId(id);
        button.setText(title);
        button.setTextColor(NAVY);
        button.setTextSize(15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(46), 1);
        windDirectionGroup.addView(button, params);
    }

    private EditText addNumberField(LinearLayout parent, String title, String hint, boolean signed) {
        parent.addView(label(title));
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setTextColor(NAVY);
        input.setHintTextColor(Color.rgb(153, 160, 180));
        input.setSingleLine(true);
        input.setPadding(dp(14), 0, dp(14), 0);
        int flags = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        if (signed) flags |= InputType.TYPE_NUMBER_FLAG_SIGNED;
        input.setInputType(flags);
        input.setBackground(rounded(Color.rgb(241, 244, 251), 12));
        LinearLayout.LayoutParams params = fullWidth();
        params.height = dp(52);
        params.bottomMargin = dp(14);
        parent.addView(input, params);
        return input;
    }

    private double number(EditText input, String emptyMessage) {
        String value = input.getText().toString().trim();
        if (value.isEmpty() || value.equals("-") || value.equals(".")) {
            throw new IllegalArgumentException(emptyMessage);
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("请输入有效数字");
        }
    }

    private LinearLayout card() {
        LinearLayout card = vertical();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(rounded(Color.WHITE, 20));
        return card;
    }

    private void addSectionTitle(LinearLayout parent, String title, String subtitle) {
        TextView heading = text(title, 20, NAVY);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        parent.addView(heading);
        TextView caption = text(subtitle, 12, MUTED);
        caption.setPadding(0, dp(3), 0, dp(16));
        parent.addView(caption);
    }

    private TextView label(String title) {
        TextView label = text(title, 13, MUTED);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setPadding(0, 0, 0, dp(6));
        return label;
    }

    private TextView text(String value, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        return view;
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radiusDp));
        return shape;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return fullWidth();
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = fullWidth();
        params.topMargin = dp(14);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
