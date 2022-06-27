#define USE_ARDUINO_INTERRUPTS true
#include <PulseSensorPlayground.h>
#include <SoftwareSerial.h>

// Habilitacion de debug para la impresion por el puerto serial ...
//----------------------------------------------
#define SERIAL_DEBUG_ENABLED 1

#if SERIAL_DEBUG_ENABLED
  #define DebugPrint(str)\
      {\
        Serial.println(str);\
      }
#else
  #define DebugPrint(str)
#endif

#define DebugPrintEstado(tipo, estado,evento)\
      {\
        String est = estado;\
        String evt = evento;\
        String type = tipo;\
        String str;\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
        DebugPrint(type);\
        str = "EST-> [" + est + "]: " + "EVT-> [" + evt + "].";\
        DebugPrint(str);\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
      }
//----------------------------------------------


#define MAX_STATES 3
#define STATE_FIRST 0
#define MIN_EVENTS 0
#define MAX_EVENTS 8
#define TIMEOUT_THRESHOLD 100
#define FREQUENCY 500
#define SERIAL_BAUDS 9600
#define MILLISECONDS_THROTTLE 2000
#define LED_APAGADO 255
#define LED_ENCENDIDO 0

//---------------------------------------------------------
//constantes

const int LED_RED_PIN = 5;
const int LED_BLUE_PIN = 6;
const int LED_GREEN_PIN = 7;

const int BUZZER_PIN = 2;

// Para bluetooth: 
// pines de transmision y recepcion
// variable para comando BOTON_PRESIONADO
// cadena de caracteres que va a contener el valor del sensor de pulso
const int BTH_RX_PIN = 10;
const int BTH_TX_PIN = 11;
const int BASE = 10;
bool esta_encendido_bth;
char buffer_bth[6]; //4 digitos + \0
// Para el botón: pin al que se conecta, y estado
const int PIN_BOTON = 3;
volatile bool esta_encendido;
bool estaba_encendido;
int valor_pulso_anterior;

bool alarma;

const int PULSE_SENSOR_PIN = 0;
PulseSensorPlayground pulseSensor;
int valor_pulso_actual;
// El rango normal de pulsaciones de un corazon en reposo
const int MIN_HEALTH_PPM = 60;
const int MAX_HEALTH_PPM = 100;

// Cotas para los rangos de estado de BPM
const int BPM_DANGER_LOW = 0;
const int BPM_WARNING_LOW = 50;
const int BPM_HEALTHY_LOW = 60;
const int BPM_HEALTHY_HIGH = 100;
const int BPM_WARNING_HIGH = 150;
const int BPM_DANGER_HIGH = 400;

const int MIN_TOP_WARNING_HEALTH_PPM = 65;
const int MIN_DOWN_WARNING_HEALTH_PPM = 55;
const int MAX_TOP_WARNING_HEALTH_PPM = 105;
const int MAX_DOWN_WARNING_HEALTH_PPM = 95;
const int NO_PULSE = 0;
const int LIMIT_PULSE = 200;

// Rango de valores de testeo
const int MIN_TEST_PPM = -10;
const int MAX_TEST_PPM = 250;

// Rango para mapeo de valores sensor de pulso
const int PULSE_THRESHOLD = 520;
//---------------------------------------------------------

/*
Inicializacion de softwareSerial para sensor bluetooth
*/
SoftwareSerial sensorBluetooth = SoftwareSerial(BTH_RX_PIN,BTH_TX_PIN);
enum comandos_bth {ENCENDER, APAGAR} comando_bth;

//---------------------------------------------------------

/*
obtencion de valor crudo desde el sensor de pulso
*/
int getHeartValue()
{
  int myBPM = pulseSensor.getBeatsPerMinute();
  return myBPM;
}

/*
evalua si el valor de pulso es irregular
*/
int isWarningValue(int heartValue)
{ // ya sabemos que no es healthy
  return heartValue >= BPM_WARNING_LOW && heartValue <= BPM_WARNING_HIGH;
}

/*
evalua si el valor de pulso es saludable
*/
int isHealthyValue(int heartValue)
{
  return heartValue >= MIN_HEALTH_PPM && heartValue <= MAX_HEALTH_PPM;
}

/*
evalua si el valor de pulso es peligroso
*/
int isDangerousValue(int heartValue)
{ // ya sabemos que no es healthy, ni warning
  return heartValue >= BPM_DANGER_LOW && heartValue <= BPM_DANGER_HIGH;
}



void Color(int R, int G, int B)
{     
     analogWrite(LED_RED_PIN , R) ;   // Red    - Rojo
     analogWrite(LED_GREEN_PIN, G) ;   // Green - Verde
     analogWrite(LED_BLUE_PIN, B) ;   // Blue - Azul
}
 
/*
enciende el led en color verde
*/
void turnOnGreenLED()
{
  Color(LED_APAGADO,LED_ENCENDIDO,LED_APAGADO);
}

/*
enciende el led en color amarillo
*/
void turnOnYellowLED()
{
  Color (LED_ENCENDIDO,LED_ENCENDIDO,LED_APAGADO);
}

/*
enciende el led en color rojo
*/
void turnOnRedLED()
{
  Color (LED_ENCENDIDO,LED_APAGADO,LED_APAGADO);
}

/*
enciende el led en color azul
*/
void turnOnBlueLED()
{
  Color (LED_APAGADO,LED_APAGADO,LED_ENCENDIDO);
}

/*
accion que se ejecuta cuando se presiona el boton
*/
void presionar_boton()
{
  esta_encendido = !esta_encendido;
}

//---------------------------------------------------------
//estados, eventos y tabla con acciones posibles
String states_s [] = {"STAND_BY" , "LECTURA_NORMAL"  ,  "ALARMA" };
enum states          { STAND_BY  ,  LECTURA_NORMAL   ,   ALARMA  } current_state;

String events_s [] = { "ESPERAR" , "ACTIVAR"  , "DESACTIVAR"   , "PULSO_NORMAL"  , "PULSO_PRECAUCION"  , "PULSO_PELIGROSO"  , "PULSO_INCORRECTO", "TIME_OUT" };
enum events          {  ESPERAR  ,  ACTIVAR   ,  DESACTIVAR    ,  PULSO_NORMAL   ,  PULSO_PRECAUCION   ,  PULSO_PELIGROSO   ,  PULSO_INCORRECTO ,  TIME_OUT  } new_event;

typedef void (*transition) ();

transition state_table[MAX_STATES][MAX_EVENTS] = 
{
  { none        , iniciar_monitoreo   , error                , error            , error             , error             , error               , error       }, //state STAND_BY
  { error       , error               , finalizar_monitoreo  , pulso_normal     , pulso_precaucion  , pulso_peligroso   , pulso_incorrecto    , error       }, //state LECTURA_NORMAL
  { error       , error               , finalizar_monitoreo  , error            , error             , error             , error               , continuar   }  //state ALARMA
 // ESPERAR     , ACTIVAR             , DESACTIVAR            , PULSO_NORMAL    , PULSO_PRECAUCION  , PULSO_PELIGROSO   , PULSO_INCORRECTO    , TIME_OUT    
};
//---------------------------------------------------------
//acciones de la tabla de estados

/*
accion ejecutada cuando se combina un estado con evento que no son validos
*/
void error()
{
  DebugPrintEstado("error", states_s[current_state], events_s[new_event]);
}

/*
accion que permite iniciar la lectura del sensor de pulsos
*/
void iniciar_monitoreo()
{
  current_state = LECTURA_NORMAL;
}

/*
accion que finaliza la lectura del sensor de pulsos
*/
void finalizar_monitoreo()
{
  Color(LED_APAGADO,LED_APAGADO,LED_APAGADO);
  noTone(BUZZER_PIN);
  alarma = false;
  
  current_state = STAND_BY;
}

/*
accion que se genera cuando la lectura del sensor de pulsos es normal.
*/
void pulso_normal()
{
  turnOnGreenLED();
  noTone(BUZZER_PIN);

  enviarBluetooth();
  current_state = LECTURA_NORMAL;
}

/*
accion que se ejecuta cuando la lectura del sensor de pulsos es irregular.
*/
void pulso_precaucion()
{
  turnOnYellowLED();
  noTone(BUZZER_PIN);

  enviarBluetooth();
  alarma = true;
  current_state = ALARMA;
}

/*
accion que se ejecuta cuando la lectura del sensor de pulsos es peligrosa.
*/
void pulso_peligroso()
{
  turnOnRedLED();
  tone(BUZZER_PIN, FREQUENCY);
  
  enviarBluetooth();
  alarma = true;
  current_state = ALARMA;
}

/*
accion que se ejecuta cuando la lectura del sensor de pulsos es incorrecta.
*/
void pulso_incorrecto()
{
  turnOnBlueLED();
  tone(BUZZER_PIN, FREQUENCY);
  
  enviarBluetooth();
  alarma = true;
  current_state = ALARMA;
}

/*
accion que permite volver a hacer lecturas del sensor de pulsos.
*/
void continuar()
{
    alarma = false;
    current_state = LECTURA_NORMAL;
}

/*
accion utilitaria para no ejecutar nada.
*/
void none()
{
    Serial.println("none");
}

//---------------------------------------------------------

void enviarBluetooth()
{
  itoa(valor_pulso_actual, buffer_bth, BASE);

  int largo = strlen(buffer_bth);
  //buffer_bth[largo] = '\n';
  buffer_bth[largo+1] = '\0';

  Serial.print("largo: ");
  Serial.println(largo);
  Serial.print("valor: ");
  Serial.println(buffer_bth);
  sensorBluetooth.write(' ');
  Serial.println(sensorBluetooth.write(buffer_bth));
  sensorBluetooth.flush();
}

//---------------------------------------------------------

bool verificar_estado_sensor_bluetooth()
{

  if(sensorBluetooth.available())
  {
    char c = sensorBluetooth.read();
    while(sensorBluetooth.available())
    {
      sensorBluetooth.read();
    }
    comando_bth = c - '0';

    switch(comando_bth)
    {
      case ENCENDER:
      case APAGAR:
        presionar_boton();
    }
  }
  

  return false;
}

/*
Se encarga de verificar el estado del boton.
Si se presiona, se enciende el dispositivo y se comienza el monitoreo.
Si se vuelve a presionar durante el monitoreo, se finaliza el mismo.
Si no se presiona, se mantiene en el estado STAND_BY.
*/
bool verificar_estado_sensor_boton()
{    
    if(esta_encendido != estaba_encendido)
    {
      estaba_encendido = esta_encendido;
      
      if(esta_encendido)
      {
          new_event = ACTIVAR;
      }
      else
      {
          new_event = DESACTIVAR;
      }
      return true;
    } else if(!esta_encendido && !estaba_encendido)
    {
      new_event = ESPERAR;
      return true;
    }

    return false;    
}

/*
se encarga de generar eventos en funcion del valor que se lea
del sensor de pulsos
*/
bool verificar_estado_sensor_pulso()
{
    valor_pulso_actual = getHeartValue();
    // no alterar el orden de llamados de función sin revisar las 
    // definiciones de is(Warning|Healthy|Dangerous)Value
    if ( isHealthyValue(valor_pulso_actual) )
    {
        new_event = PULSO_NORMAL;
    }
    else if( isWarningValue(valor_pulso_actual) )
    {
        new_event = PULSO_PRECAUCION;
    }
    else if( isDangerousValue(valor_pulso_actual) )
    {
        new_event = PULSO_PELIGROSO;
    }
    else
    {
        new_event = PULSO_INCORRECTO;
    }
}

/*
Se encarga de verificar que si no se genera ningun evento, 
se genere un time_out, para retornar al estado LECTURA_NORMAL,
para volver a hacer lecturas al sensor.
*/
bool verificar_alarma()
{
    if(alarma) {
        new_event = TIME_OUT;
        return true;
    }

    return false;
}

/*
se encarga de evaluar los diferentes sensores para obtener un evento
*/
void get_new_event()
{

  if(verificar_estado_sensor_bluetooth() || verificar_estado_sensor_boton() || verificar_alarma() || verificar_estado_sensor_pulso()) {
    return;
  }
}

void setup()
{
  pinMode(LED_GREEN_PIN, OUTPUT);
  pinMode(LED_RED_PIN, OUTPUT);
  pinMode(LED_BLUE_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(PIN_BOTON, INPUT_PULLUP);
  pinMode(BTH_RX_PIN, INPUT);
  pinMode(BTH_TX_PIN, OUTPUT);

  attachInterrupt(digitalPinToInterrupt(PIN_BOTON), presionar_boton, FALLING);

  pulseSensor.analogInput(PULSE_SENSOR_PIN);
  pulseSensor.setThreshold(PULSE_THRESHOLD);
  if(pulseSensor.begin())
  {
    Serial.println("Se inició el sensor de pulso!");
  }
  
  Serial.begin(SERIAL_BAUDS);
  sensorBluetooth.begin(SERIAL_BAUDS);
  esta_encendido_bth = false;

  esta_encendido = false;
  estaba_encendido = false;
  valor_pulso_anterior = BPM_DANGER_LOW;
  alarma = false;
  Color(LED_APAGADO,LED_APAGADO,LED_APAGADO);

  current_state = STAND_BY;
}

void loop()
{
  if ( ( millis() % MILLISECONDS_THROTTLE) != 0 )
  {
    // de esta forma no es afectado por el timeout al ocurrir 
    // el overflow de unsigned long luego de aprox 50 días
    return;
  }
  
  get_new_event();

  if( (new_event >= MIN_EVENTS) && (new_event < MAX_EVENTS) && ( current_state >= STATE_FIRST ) && (current_state < MAX_STATES) )
  {
    DebugPrintEstado("Log", states_s[current_state], events_s[new_event]);
    state_table[current_state][new_event]();
  }
}
