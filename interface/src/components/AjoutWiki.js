import axios from "axios";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL, APP_URL } from '../config';
import MDEditor from "@uiw/react-md-editor";

function AjoutWiki() {
    const { nomParDefaut } = useParams();
    const [nom, setNom] = useState(nomParDefaut);
    const [image, setImage] = useState('');
    const [mdImage, setMdImage] = useState('');
    const [description, setDescrition] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const nav = useNavigate();

    const handleRetourClick = () => {
        nav(-1);
    };

    const majNom = (e) => {
        setNom(e.target.value);
    };

    const majImage = (e) => {
        setImage(e.target.value);
        setMdImage('![Veuillez insérer une image valide]('+e.target.value+')');
    };
    
    

    const majDesc = (e) => {
        setDescrition(e.target.value);
    };
    const connect=()=>{
        nav('/account/connect');
    }

    function handleAddWiki() {
        const requestData = {
            nom: nom,
            description: description,
            imageBackground : image,
            adminId : localStorage.getItem('account')
        };
        if (nom.trim().length === 0 || description.trim().length === 0) {
            setErrorMessage("Veuillez compléter les champs textuels");
        } else {
            setErrorMessage("");
            axios.post( API_URL+'/wiki/create', requestData).then((response) => {
                alert('Wiki créé avec succès');
                setTimeout(() => {
                    window.location.href = `${APP_URL}/wiki/${response.data._id}`;
                }, 1);
            }).catch((error) => {
                console.error("Erreur lors de la création du wiki :", error);
            });
        }
    }

    return (
        <div>
          {localStorage.getItem('account') === null ? (
            <div>
                <h2>Vous devez être connecté pour créer un wiki</h2>
                <br/>
                <button onClick={connect}>Se connecter</button>
            </div>
          ) : (
            <div class="flex-down">
              <h2>Créer un wiki :</h2>
              <div>
                <input type="text" placeholder="Nom" value={nom} onChange={majNom} />
              </div>
              <textarea rows="10" placeholder="Description" onChange={majDesc} />
              <input type="text" placeholder="Lien de l'image de fond" value={image} onChange={majImage} />
              <MDEditor.Markdown source={mdImage}/>
              <div>
                  <button class="button-highlight" onClick={handleAddWiki}>Valider</button>
                  <button style={{ cursor: 'pointer' }} onClick={handleRetourClick}>Retour</button>
              </div>
              <p>{errorMessage}</p>
            </div>
          )}
        </div>
      );
      
}

export default AjoutWiki;
