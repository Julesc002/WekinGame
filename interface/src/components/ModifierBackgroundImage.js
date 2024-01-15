import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL, APP_URL } from '../config';
import BackgroundWiki from "./BackgroundWiki";
import MDEditor from "@uiw/react-md-editor";

function ModifierBackgroundImage() {
    const { wikiId } = useParams();
    const [lienImage, setLienImage] = useState('');
    const [mdImage, setMdImage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [wiki, setWiki] = useState(null);
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    const majLienImage = (e) => {
        setLienImage(e.target.value);
        setMdImage('![Veuillez insérer une image]('+e.target.value+')');
    };

    useEffect(() => {
        axios.get(`${API_URL}/wiki/${wikiId}/content/-1`).then((res) => {
            setWiki(res.data);
        }).catch((error) => {
            console.error(error);
        });
    }, [wikiId]);

    const isUserOwner = () => {
        if (localStorage.getItem('account') !== null && wiki) {
            return parseInt(localStorage.getItem('account')) === wiki.owner;
        }
        return false;
    }

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
            {isUserOwner() ? (
                <div>
                    <BackgroundWiki id={wikiId} />
                    <input type="text" placeholder="Lien de l'image de fond" onChange={majLienImage} />
                    <button onClick={handleUpdateBackgroundImage}>Confirmer</button>
                    <p>{errorMessage}</p>
                    <button style={{ cursor: 'pointer' }} onClick={handleRetourClick}>Retour</button>
                    <MDEditor.Markdown source ={mdImage}/>
                </div>
            ) : (
                <div>
                    <h1>Vous n'avez pas les droits pour modifier l'image de fond de ce wiki</h1>
                </div>
            )}
        </div>
    );
}

export default ModifierBackgroundImage;