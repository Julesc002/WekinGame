import axios from "axios";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL, APP_URL } from '../config';
import BackgroundWiki from "./BackgroundWiki";

function ModifierBackgroundImage() {
    const { wikiId } = useParams();
    const [lienImage, setLienImage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    const majLienImage = (e) => {
        setLienImage(e.target.value);
    };

    function handleUpdateBackgroundImage() {

        const requestData = {
            image: lienImage
        };
        if (lienImage.trim().length === 0) {
            setErrorMessage("Veuillez remplir le champ");
        } else {
            setErrorMessage("");
            axios.patch( API_URL+'/wiki/'+ wikiId + '/background', requestData).then((response) => {
                window.location.href = `${APP_URL}/wiki/${wikiId}`;
            }).catch((error) => {
                console.error("Erreur lors de la modification de la background image :", error);
            });
        }
    }

    return (
        <div>
            <BackgroundWiki id={wikiId} />
            <input type="text" placeholder="Insérer nom" onChange={majLienImage} />
            <button onClick={handleUpdateBackgroundImage}>Confirmer</button>
            <p>{errorMessage}</p>
            <button style={{ cursor: 'pointer' }} onClick={handleRetourClick}>Retour</button>
        </div>
    );
}

export default ModifierBackgroundImage;