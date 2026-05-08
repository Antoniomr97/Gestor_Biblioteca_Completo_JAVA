package biblioteca.utilidades;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * Clase de herramientas para trabajar con XML usando la técnica DOM
 * (Document Object Model). DOM nos permite tratar un fichero XML como
 * un árbol de nodos que podemos leer y modificar en memoria.
 *
 * Todos sus métodos son estáticos: no hace falta crear un objeto de esta clase,
 * la llamamos directamente → UtilidadesXML.xmlToDom(...).
 *
 * Métodos obligatorios del enunciado:
 *  - xmlToDom   → leer un XML y convertirlo en árbol DOM
 *  - domToXml   → guardar un árbol DOM como fichero XML
 *  - crearDomVacio → crear un árbol DOM vacío listo para rellenar
 */
public class UtilidadesXML {

    // Ocultamos el constructor para que nadie pueda crear objetos de esta clase.
    // Es una clase de herramientas puras, como Math en Java.
    private UtilidadesXML() {}

    // ============================================================
    /**
     * Lee un fichero XML del disco y lo convierte en un árbol DOM
     * que podemos recorrer y consultar desde Java.
     *
     * Flujo: fichero en disco → DocumentBuilder parsea → Document (árbol DOM)
     *
     * @param ruta Ruta al fichero XML (puede ser absoluta o relativa).
     * @return El Document DOM ya cargado, o null si el fichero no existe o tiene errores.
     */
    // ============================================================
    public static Document xmlToDom(String ruta) {
        try {
            // Comprobamos que el fichero existe antes de intentar abrirlo
            File fichero = new File(ruta);
            if (!fichero.exists()) {
                System.err.println("UtilidadesXML.xmlToDom: El fichero no existe → " + ruta);
                return null;
            }

            // Creamos el motor que sabe leer XML (DocumentBuilder)
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Le decimos que ignore espacios en blanco entre etiquetas para un árbol más limpio
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parseamos (leemos y convertimos) el fichero XML en un árbol DOM
            Document doc = builder.parse(fichero);

            // Normalizamos el árbol: combina nodos de texto adyacentes y elimina los vacíos
            doc.getDocumentElement().normalize();
            return doc;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            // Si el XML está mal formado o hay error de lectura, avisamos y devolvemos null
            System.err.println("UtilidadesXML.xmlToDom: Error al parsear el XML → " + e.getMessage());
            return null;
        }
    }

    // ============================================================
    /**
     * Convierte un árbol DOM que tenemos en memoria en un fichero XML
     * guardado en el disco (serialización).
     *
     * Flujo: Document (árbol DOM) → Transformer serializa → fichero XML en disco
     *
     * @param dom  El árbol DOM que queremos guardar.
     * @param ruta La ruta donde crearemos el fichero XML.
     */
    // ============================================================
    public static void domToXml(Document dom, String ruta) {
        try {
            // El Transformer es el encargado de convertir el DOM en texto XML
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // Configuramos el formato de salida: con indentación y codificación UTF-8
            transformer.setOutputProperty(OutputKeys.INDENT,     "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING,   "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            // Indicamos que cada nivel del XML se indente 4 espacios para que sea legible
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // DOMSource envuelve nuestro árbol DOM como fuente de datos
            DOMSource source = new DOMSource(dom);
            // StreamResult indica dónde escribimos el resultado (el fichero de destino)
            StreamResult result = new StreamResult(new File(ruta));

            // Realizamos la transformación: DOM → fichero XML
            transformer.transform(source, result);

            System.out.println("UtilidadesXML.domToXml: Fichero guardado correctamente → " + ruta);

        } catch (TransformerException e) {
            // Si falla la escritura, lanzamos una excepción clara para que la capa superior la gestione
            System.err.println("UtilidadesXML.domToXml: Error al guardar el XML → " + e.getMessage());
            throw new RuntimeException("Error al escribir el fichero XML: " + ruta, e);
        }
    }

    // ============================================================
    /**
     * Crea un documento DOM completamente vacío con solo la etiqueta raíz indicada.
     * Lo usamos como punto de partida antes de añadir los datos que queremos exportar.
     *
     * Ejemplo: crearDomVacio("usuarios") genera → <usuarios></usuarios>
     *
     * @param etiquetaRaiz El nombre de la etiqueta principal del XML.
     * @return Un Document listo para recibir nodos hijo.
     */
    // ============================================================
    public static Document crearDomVacio(String etiquetaRaiz) {
        try {
            // Usamos el mismo motor de parseo para crear un documento nuevo vacío
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Creamos la etiqueta raíz y la añadimos como base del documento
            doc.appendChild(doc.createElement(etiquetaRaiz));
            return doc;

        } catch (ParserConfigurationException e) {
            System.err.println("UtilidadesXML.crearDomVacio: Error al crear el DOM → " + e.getMessage());
            throw new RuntimeException("Error al crear el documento DOM vacío", e);
        }
    }
}
