package com.example.myrental;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CreateAgreement extends AppCompatActivity {

    ImageView imageView;
    EditText title, body;
    LinearLayout linearLayout;
    Button button;
    TextView First, Second, Third, Fourth, Fifth, Sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_agreement);

        title = findViewById(R.id.title);
        linearLayout = findViewById(R.id.body);
        button = findViewById(R.id.button);
        imageView = findViewById(R.id.image);
        Sign = findViewById(R.id.sign);

        final String Date = "01/04/2020";
        final String City = "Belagavi";
        final String Name = "Rushabh Rajesh Jain";
        final Integer Age = 56;
        final String Occupation = "Business";
        final String PAN = "BOBPD2763R";
        final String UID = "757764558602";
        final String Address = "#1690 2/3, Market Galli, near Bhaji Market, Jamkhandi - 587 301";

        First = findViewById(R.id.First);
        Second = findViewById(R.id.Second);
        Third = findViewById(R.id.Third);
        Fourth = findViewById(R.id.Fourth);
        Fifth = findViewById(R.id.Fifth);
        First.setText(Html.fromHtml("Draft of Leave and License Agreement<br><br> This Agreement is made and executed on <b><u>"+Date+"</b></u> at <b><u>"+City+"</b></u>, between <br>"));
        Second.setText(Html.fromHtml("       Mr. <b><u>"+Name+"</b></u> Age: About "+Age+" Years, Occupation: <b><u>"+Occupation+"</b></u>, PAN: <b><u>"+PAN+"</b></u>, UID: <b><u>"+UID+"</b></u>, Residing at: <b><u>"+Address+"</b></u>, HEREINAFTER called the Licensor (which expression shall mean and include the Licensor above named as also his respective heirs, successors, assigns, executors and administrators)<br>"));
        Third.setText("And\n");
        Fourth.setText(Html.fromHtml("       Mr. <b><u>"+Name+"</b></u> Age: About "+Age+" Years, Occupation: <b><u>"+Occupation+"</b></u>, PAN: <b><u>"+PAN+"</b></u>, UID: <b><u>"+UID+"</b></u>, Residing at: <b><u>"+Address+"</b></u>, HEREINAFTER called the Licensor (which expression shall mean and include the Licensor above named as also his respective heirs, successors, assigns, executors and administrators)<br>"
                +"     Whereas the Licensor is the owner of a piece of land at "+PAN+" bearing Survey No <b><u>"+PAN+"</b></u> with a building consisting of <b><u>"+PAN+"</b></u> floor <b><u>"+PAN+"</b></u> having built up area of about "
                +PAN+" square feet.<br><br>     And Whereas the Licensee has approached the licensor with a request to allow the Licensee to temporarily occupy and use a portion of the <b><u>"+PAN+"</b></u>"+
                " floor of the said building, admeasuring about <b><u>"+PAN+"</b></u> square feet for carrying on his <b><u>"+PAN+"</b></u> on leave and license basis until the Licensee gets other more suitable accommodation.<br><br>" +
                "     And Whereas the Licensor has agreed to grant leave and license to the Licensee to occupy and use the said ground floor portion of the said building and which portion is shown on the" +
                " plan hereto annexed by red boundary line on the following terms and conditions agreed to between the parties hereto;<br><br>Now it is agreed by and between the parties hereto as follows..<br><br>"
                +"1. The Licensor hereby grants leave and license to the Licensee to occupy and use the said portion of the <b><u>"+PAN+"</b></u> floor of the said building of the Licensor (hereinafter referred to as the Licensed Premises) for a period of <b><u>"+PAN+"</b></u> months from <b><u>"+PAN+"</b></u> The Licensee agrees to vacate the said premises even earlier If the Licensee secures any other accommodation in the locality where the said premises are situated.<br><br>"
                +"2. The Licensee shall pay to the Licensor a sum of Rs <b><u>"+PAN+"</b></u> per month (calculated at the rate of Rs <b><u>"+PAN+"</b></u> per square foot) as License fee or compensation to be paid in advance for each month on or before the <b><u>"+PAN+"</b></u> day of each month.<br><br>"+
                "3. All the Municipal taxes and other taxes and levies in respect of the licensed premises will be paid by the Licensor alone.<br><br>" +
                "4. The electric charges and water charges for electric and water consumption in the said licensed premises will be paid by the Licensee to the authorities concerned and the Licensor will not be responsible for the same. For the sake of convenience a separate electric and water meter if possible will be provided in the said premises.<br><br>" +
                "5. The Licensee will be allowed to use the open space near the entrance to the Licensed premises and shown on the said plan by green wash for parking cars during working hours of the Licensee and not for any other time and no car or other vehicle will be parked on any other part of the said plot.<br><br>" +
                "6. The licensed premises will be used only for carrying on business and for no other purpose.<br><br>" +
                "7. The licensed premises have normal electricity fittings and fixtures. If the Licensee desires to have any additional fittings and fixtures, the Licensee may do so at his cost and in compliance with the rules. The Licensee shall remove such fittings and fixtures on the termination of the license failing which they shall be deemed to be the property of the Licensor.<br><br>" +
                "8. The licensed premises are given to the Licensee on personal basis and the Licensee will not be entitled to transfer the benefit of this agreement to anybody else or will not be entitled to allow anybody else to occupy the premises or any part thereof. Nothing in this agreement shall be deemed to grant a lease and the licensee agrees and undertakes that no such contention shall be taken up by the Licensee at any time.<br><br>" +
                "9. The Licensee shall not be deemed to be in the exclusive occupation of the licensed premises and the Licensor will have the right to enter upon the premises at any time during working hours to inspect the premises.<br><br>" +
                "10. The Licensee shall maintain the licensed premises in good condition and will not cause any damage thereto. If any damage is caused to the premises or any part thereof by the Licensee or his employees, servants or agents the same will be made good by the Licensee at the cost of the Licensee either by rectifying the damage or by paying cash compensation as may be determined by the Licensor's Architect.<br><br>" +
                "11. The Licensee shall not carry out any work of structural repairs or additions or alterations to the said premises. Only such alterations or additions as are not of structural type or of permanent nature may be allowed to be made by the Licensee inside the premises with the previous permission of the Licensor.<br><br>" +
                "12. The Licensee shall not cause any nuisance or annoyance to the people-in the neighbourhood or store any hazardous goods on the premises.<br><br>" +
                "13. If the Licensee commits a breach of any term of this agreement then notwithstanding anything herein contained the Licensor will be entitled to terminate this agreement by fifteen days' prior notice to the Licensee.<br><br>" +
                "14. On the expiration of the said term or period of the License or earlier termination thereof, the Licensee shall hand over vacant and peaceful possession of the Licensed premises to the Licensor In the same condition In which the premises now exist subject to normal wear and tear. The Licensee's occupation of the premises after such termination will be deemed to be that of a trespasser.<br><br>" +
                "IN WITNESS WHEREOF the parties hereto have put their hands the day and year first hereinabove written.<br><br>Signed by the within named Licensor Shri <b>"+Name+"</b>"));
        Fifth.setText(Html.fromHtml("Signed by the within named Licensor Shri <b>"+Name+"</b>"));


        Sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleT = title.getText().toString();
                String bodyT = body.getText().toString();
                String path = getExternalFilesDir(null).toString()+"/"+titleT+".pdf";

                File file = new File(path);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Document document = new Document(PageSize.A4);

                    try {
                        PdfWriter.getInstance(document, new FileOutputStream(file.getAbsoluteFile()));
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    document.open();
                    try {
                        Drawable d = getResources().getDrawable(R.drawable.img);
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
                        Bitmap bitmap = bitmapDrawable.getBitmap(); //BitmapFactory.decodeStream(i);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        Image image = Image.getInstance(stream.toByteArray());
                        image.scaleAbsolute(35,15);

                        PdfPTable table = new PdfPTable(1);
                        table.setWidthPercentage(100);
                        //table.setWidths(new int[]{14,1});
                        PdfPCell cell = new PdfPCell();
                        Paragraph p10 =  new Paragraph("Draft of Leave and License Agreement \n\n This Agreement is made and executed on ");
                        p10.add(new Chunk(Date).setUnderline(0.7f,-3f));
                        p10.add(" at ");
                        p10.add(new Chunk(City).setUnderline(0.7f,-3f));
                        p10.add(", between \n\n");
                        p10.setAlignment(Element.ALIGN_CENTER);
                        cell.addElement(p10);

                        Paragraph p = new Paragraph("       Mr. ");
                        p.add(new Chunk(Name).setUnderline(0.7f,-2.5f));
                        p.add(", Age: About ");
                        p.add(new Chunk(""+Age).setUnderline(0.7f,-3f));
                        p.add(" Years, Occupation: ");
                        p.add(new Chunk(Occupation).setUnderline(0.7f,-3f));
                        p.add(", PAN: ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(", UID: ");
                        p.add(new Chunk(UID).setUnderline(0.7f,-3f));
                        p.add(", Residing at: ");
                        p.add(new Chunk(Address).setUnderline(0.7f,-3f));
                        p.add(", HEREINAFTER called the Licensor (which expression shall mean and include the Licensor above named as also his respective heirs, successors, assigns, executors and administrators)\n\n");

                        p.add("                                                                      AND                                                 \n\n");

                        p.add("     Mr. ");
                        p.add(new Chunk(Name).setUnderline(0.7f,-2.5f));
                        p.add(", Age: About ");
                        p.add(new Chunk(""+Age).setUnderline(0.7f,-3f));
                        p.add(" Years, Occupation: ");
                        p.add(new Chunk(Occupation).setUnderline(0.7f,-3f));
                        p.add(", PAN: ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(", UID: ");
                        p.add(new Chunk(UID).setUnderline(0.7f,-3f));
                        p.add(", Residing at: ");
                        p.add(new Chunk(Address).setUnderline(0.7f,-3f));
                        p.add(", HEREINAFTER called the Licensor (which expression shall mean and include the Licensor above named as also his respective heirs, successors, assigns, executors and administrators)\n\n");

                        p.add("     Whereas the Licensor is the owner of a piece of land at ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" bearing Survey No ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" with a building consisting of ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" floor ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" having built up area of about ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" square feet.\n\n");
                        p.add("     And Whereas the Licensee has approached the licensor with a request to allow the Licensee to temporarily occupy and use a portion of the");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" floor of the said building, admeasuring about ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" square feet for carrying on his ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" on leave and license basis until the Licensee gets other more suitable accommodation.\n\n");
                        p.add("      And Whereas the Licensor has agreed to grant leave and license to the Licensee to occupy and use the said ground floor portion of the said building and which portion is shown on the plan hereto annexed by red boundary line on the following terms and conditions agreed to between the parties hereto;\n\n");
                        p.add("Now it is agreed by and between the parties hereto as follows..\n\n");
                        p.add("1. The Licensor hereby grants leave and license to the Licensee to occupy and use the said portion of the ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add("floor of the said building of the Licensor (hereinafter referred to as the Licensed Premises) for a period of ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" months from ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" The Licensee agrees to vacate the said premises even earlier If the Licensee secures any other accommodation in the locality where the said premises are situated.\n\n\n");
                        p.add("2. The Licensee shall pay to the Licensor a sum of Rs ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" per month (calculated at the rate of Rs ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" per square foot) as License fee or compensation to be paid in advance for each month on or before the ");
                        p.add(new Chunk(PAN).setUnderline(0.7f,-3f));
                        p.add(" day of each month.\n\n");
                        p.setAlignment(Element.ALIGN_JUSTIFIED);


                        Paragraph p1 = new Paragraph(body.getText().toString());
                        p1.setAlignment(Element.ALIGN_JUSTIFIED);

                        cell.addElement(p);
                        cell.addElement(p1);
                        Paragraph p2 = new Paragraph("Signed by the within named Licensor Shri "+Name+"\n\n");
                        p2.setAlignment(Element.ALIGN_RIGHT);
                        cell.addElement(p2);
                        Chunk chunk1 = new Chunk(image, 450f, 0f, true);
                        cell.addElement(chunk1);
                        Paragraph p3 = new Paragraph("Signed by the within named License Shri "+Name+"\n\n");
                        p3.setAlignment(Element.ALIGN_RIGHT);
                        cell.addElement(p3);
                        Chunk chunk2 = new Chunk(image, 450f, 0f, true);
                        cell.addElement(chunk2);
                        cell.addElement(new Paragraph("\n\n\n\n\n"));
                        cell.setPadding(10);
                        table.addCell(cell);

                        document.add(table);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        return;
                    }
                    Toast.makeText(CreateAgreement.this, "Exported Successfully", Toast.LENGTH_SHORT).show();
                    document.close();
                }
                else
                {
                    Toast.makeText(CreateAgreement.this, "Already Exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Drawable drawable = Drawable.createFromStream(inputStream, data.getData().toString());
                Sign.setBackground(drawable);
            }
            catch (IOException e)
            {

            }
        }
    }
}
