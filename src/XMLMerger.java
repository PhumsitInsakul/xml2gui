import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class XMLMerger {

    private static final Set<String> duplicateAllowedFields = new HashSet<>(Arrays.asList(
            "LetterOfGuaranteeAssetDetail", "NotGenerate", "ExistingGuaranteeCollateralDetails", "needPaymentForAccruedInterestAmount", "isCustWillReqForDrawdownAfterCreditLineDecrease",
            "AuthorizedPersonToSignContract", "Exe1VerifiedCorrect", "LeaseholdAssetDetail", "DocumentProperty", "SecurityDetail",
            "StepRate", "JointVentureOrConsortiumProfiles", "PersonalOrJuristicProfiles", "FeePaymentMethod", "TFCCustomerNum",
            "DGENSupport", "MortgageRank", "LegalActivityDetail", "feeContractReference", "CreditLineAccountInfo",
            "StockAssetDetail", "ShipAssetDetail", "OwnerAuthPersonToPerformLegalActDetail", "CodeNameMappings", "LeaseholdAssetSubDetails",
            "CarAssetDetail", "ExistingGuaranteeContractDetails", "BillExchangeAssetDetail", "ExistingLegalActType", "CustomerNameAndGUID",
            "TemplateRefValue", "InstallmentEveryMonth", "CommoditiesAssetDetail", "Locations", "PackageFinanceDetails",
            "AddExternalError", "Sequence", "TemplateKeyGUID", "TemplateDetail", "GoldAssetDetail",
            "BillingScheduleTypes", "ALSCustomerNum", "CondominiumAssetDetail", "TemplateSubLevel", "TransferTo",
            "BillInfos", "isSME", "needPaymentForAccruedInterest", "Covenants", "RightOnBenefitAssetDetail",
            "CommercialCollateralContractDetail", "authorizedPersonToPerformLegalAct", "AuthorizedPerson", "ISupplyInfo", "PNDuePayments",
            "CollateralDetail", "CustIdentification", "LeasingAssetDetail", "OtherFeeInfo", "PaymentStep",
            "FeeInfo", "BondAssetDetail", "RemarkForAdditionalDocument", "InterestODInfo", "MachineAssetDetail",
            "LinkageNoInCase", "ProjectNameSoftLoans", "RightDebtors", "RepaymentTransactionInfos", "ExistingLegalActivity",
            "specialLoans", "FeeListDetail", "TransactionDetail", "IsNonstandardContract", "LandBuildingAssetDetail",
            "AccountInfo", "SpecificDebtInContract", "InterestRateValueAsOfDates", "SplitOfShareCertificateDetail", "SpecialLoanTypes",
            "PeriodInstallment", "MachineRegistrationNos", "ProcessAgentInfos", "CreditLineFees", "PensionAssetDetail",
            "GuaranteeGroupDetail", "Date", "GuaranteeExistingContract", "DisbursementInfo", "ApplicationDetail",
            "GuaranteeDetail", "KeeperDetails", "IsProcessIncreaseAndExtendCreditLine", "Paragraphs", "ContractLanguage"
    ));

    public static void main(String[] args) {
        try {
            // Load clean-xml.xml
            File cleanFile = new File("C:\\Installer\\XMLToMerge\\EmptyXML\\clean-xml.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Enable namespace awareness
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document cleanDoc = builder.parse(cleanFile);

            // Load 1398020.xml
            File sourceFile = new File("C:\\Installer\\XMLToMerge\\FilledXML\\1398020.xml");
            Document sourceDoc = builder.parse(sourceFile);

            // Merge the entire document
            Node sourceRoot = sourceDoc.getDocumentElement();
            Node cleanRoot = cleanDoc.getDocumentElement();

            mergeNodes(cleanDoc, cleanRoot, sourceRoot);

            // Save the merged XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "none");

            DOMSource domSource = new DOMSource(cleanDoc);
            StreamResult result = new StreamResult(new File("C:\\Installer\\XMLToMerge\\Finished\\merged-clean-xml.xml"));
            transformer.transform(domSource, result);

            System.out.println("Merge completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void mergeNodes(Document cleanDoc, Node cleanNode, Node sourceNode) {
        NodeList sourceChildren = sourceNode.getChildNodes();

        for (int i = 0; i < sourceChildren.getLength(); i++) {
            Node sourceChild = sourceChildren.item(i);

            if (sourceChild.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = sourceChild.getNodeName();

                // ตรวจสอบว่าเป็น Field ที่ต้อง Duplicate
                if (duplicateAllowedFields.contains(tagName)) {
                    int sourceCount = countChildrenWithData(sourceNode, tagName);
                    int cleanCount = countChildrenWithData(cleanNode, tagName);

                    boolean didDuplicate = false;

                    // Duplicate Field ที่จำเป็น
                    if (sourceCount > cleanCount) {
                        duplicateField(cleanDoc, cleanNode, tagName, sourceCount - cleanCount);
                        didDuplicate = true;
                    }
                    // แก้ให้ removeDuplicateNodes เมื่อ node นั้นๆ มีการ action duplicateField เท่านั้น

                    // เติมข้อมูลใน Field
                    mergeDuplicateFields(cleanDoc, cleanNode, sourceNode, tagName);

                    // ลบเฉพาะต้นฉบับที่ Duplicate
                    if (didDuplicate) {
                        removeDuplicateNodes(cleanNode, tagName);
                    }
                } else {
                    Node correspondingCleanNode = findChildNode(cleanNode, tagName);

                    if (correspondingCleanNode != null) {
                        // Merge recursively
                        mergeNodes(cleanDoc, correspondingCleanNode, sourceChild);
                    } else {
                        // Append new node if it does not exist in cleanNode
                        Node importedNode = cleanDoc.importNode(sourceChild, true);
                        cleanNode.appendChild(importedNode);
                    }
                }
            } else if (sourceChild.getNodeType() == Node.TEXT_NODE) {
                if (!sourceChild.getTextContent().trim().isEmpty()) {
                    cleanNode.setTextContent(sourceChild.getTextContent().trim());
                }
            }
        }
    }

    private static void removeDuplicateNodes(Node parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName)) {
                // ตรวจสอบว่าโหนดว่างเปล่าหรือซ้ำซ้อน
                if (isNodeEmpty(child)) {
                    parent.removeChild(child);
                    i--; // ปรับ index หลังจากลบ
                }
            }
        }
    }

    private static boolean isNodeEmpty(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE && !child.getTextContent().trim().isEmpty()) {
                return false; // โหนดมีข้อมูล
            }
        }
        return true; // โหนดว่างเปล่า
    }

    // ฟังก์ชัน Duplicate Field
    private static void duplicateField(Document cleanDoc, Node cleanNode, String tagName, int count) {
        Node templateNode = findChildNode(cleanNode, tagName);

        if (templateNode != null) {
            for (int i = 0; i < count; i++) {
                Node duplicateNode = cleanDoc.importNode(templateNode, true);
                cleanNode.appendChild(duplicateNode);
            }
        }
    }

    // ฟังก์ชันเติมข้อมูลใน Field ที่ Duplicate
    private static void mergeDuplicateFields(Document cleanDoc, Node cleanNode, Node sourceNode, String tagName) {
        NodeList cleanChildren = cleanNode.getChildNodes();
        NodeList sourceChildren = sourceNode.getChildNodes();

        int cleanIndex = 0;

        for (int i = 0; i < sourceChildren.getLength(); i++) {
            Node sourceChild = sourceChildren.item(i);

            if (sourceChild.getNodeType() == Node.ELEMENT_NODE && sourceChild.getNodeName().equals(tagName)) {
                while (cleanIndex < cleanChildren.getLength()) {
                    Node cleanChild = cleanChildren.item(cleanIndex);

                    if (cleanChild.getNodeType() == Node.ELEMENT_NODE && cleanChild.getNodeName().equals(tagName)) {
                        mergeNodes(cleanDoc, cleanChild, sourceChild);
                        cleanIndex++;
                        break;
                    }
                    cleanIndex++;
                }
            }
        }
    }

    // ฟังก์ชันตรวจสอบจำนวน Field ที่มีข้อมูล
    private static int countChildrenWithData(Node parent, String tagName) {
        NodeList children = parent.getChildNodes();
        int count = 0;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeName().equals(tagName) && hasNonEmptyChild(child)) {
                count++;
            }
        }
        return count;
    }

    // ฟังก์ชันตรวจสอบว่ามีข้อมูลใน Node หรือไม่
    private static boolean hasNonEmptyChild(Node node) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (!child.getTextContent().trim().isEmpty() || hasNonEmptyChild(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ฟังก์ชันค้นหา Node ตามชื่อ
    private static Node findChildNode(Node parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(tagName)) {
                return child;
            }
        }
        return null;
    }
}


