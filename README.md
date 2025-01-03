# DynamicXMLTreeEditor

**DynamicXMLTreeEditor** เป็นโปรแกรมแก้ไขไฟล์ XML ในรูปแบบ Tree Structure ด้วย GUI ที่ใช้งานง่าย รองรับการเพิ่ม ลบ แก้ไขค่า Undo/Redo และการจัดการไฟล์ XML โดยไฟล์ `.jar` และ `.class` พร้อมสำหรับใช้งาน และเตรียมแปลงเป็น `.exe` ในขั้นถัดไป

## ฟีเจอร์หลัก
- **Tree Structure:** แสดงผลโครงสร้างไฟล์ XML เป็นรูปแบบ Tree  
- **เพิ่ม/ลบ/แก้ไขฟิลด์:** ปรับแต่ง Field Name และ Value ของ XML ได้ง่าย  
- **Undo/Redo:** รองรับการย้อนกลับหรือกู้คืนการเปลี่ยนแปลง  
- **การจัดการไฟล์:** โหลดไฟล์ XML และบันทึกไฟล์ที่แก้ไข  
- **GUI ที่ใช้งานง่าย:** ออกแบบด้วย Swing พร้อมปุ่มฟังก์ชันที่ครบถ้วน  

## วิธีการใช้งาน
1. **รันโปรแกรม:**  
   - รันไฟล์ `.jar`: `java -jar DynamicXMLTreeEditor.jar`  
   - หรือรันไฟล์ `.class`:  
     ```bash
     javac DynamicXMLTreeEditor.java  
     java DynamicXMLTreeEditor  
     ```  
2. **ฟังก์ชันในโปรแกรม:**  
   - **Load XML:** คลิกปุ่ม `Load XML` เพื่อเลือกไฟล์ XML  
   - **เพิ่มฟิลด์:** ใส่ Field Name และ Value แล้วคลิก `Add Subfield`  
   - **ลบฟิลด์:** เลือกฟิลด์ที่ต้องการลบ แล้วคลิก `Delete Field`  
   - **Undo/Redo:** ใช้ปุ่ม `Undo` หรือ `Redo` เพื่อย้อนกลับหรือกู้คืน  
   - **บันทึกไฟล์:** คลิก `Save File` เพื่อบันทึกไฟล์ XML  

## ตัวอย่าง XML ที่รองรับ
```xml
<Root>
    <Person>
        <Name>John Doe</Name>
        <Age>30</Age>
        <Address>
            <Street>Main St</Street>
            <City>New York</City>
        </Address>
    </Person>
</Root>
```

Tree ที่แสดงในโปรแกรม:  
```
Root  
└── Person  
    ├── Name  
    ├── Age  
    └── Address  
        ├── Street  
        └── City  
```

## แผนการพัฒนา 
- **แปลงจาก WSDL เป็น XML:** เพิ่มฟีเจอร์การแปลงไฟล์ WSDL เป็น XML โดยตรงภายในแอป เพื่อให้ผู้ใช้ไม่ต้องใช้เครื่องมือแปลงภายนอก
- แปลงโปรแกรมเป็น `.exe` ด้วย Launch4j  
- เพิ่มการรองรับ XML Attributes  
- ปรับปรุง UI ให้สวยงามและยืดหยุ่นมากขึ้น  
- รองรับโครงสร้าง XML ขนาดใหญ่และซับซ้อน

## ความต้องการระบบ
- **Java Runtime Environment (JRE)** เวอร์ชัน 8 ขึ้นไป  
- ระบบปฏิบัติการ: Windows, macOS, หรือ Linux  

---

**หมายเหตุ:** หากพบปัญหาหรือมีข้อเสนอแนะ ติดต่อผ่าน [GitHub Issues](https://github.com/username/DynamicXMLTreeEditor/issues)  
